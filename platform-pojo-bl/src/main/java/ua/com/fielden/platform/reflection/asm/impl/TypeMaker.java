package ua.com.fielden.platform.reflection.asm.impl;

import static java.lang.String.format;
import static java.util.stream.Collectors.toCollection;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.ParameterManifestation;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition;
import net.bytebuddy.dynamic.TargetType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;
import ua.com.fielden.platform.entity.Mutator;
import ua.com.fielden.platform.entity.annotation.Generated;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.factory.ObservableAnnotation;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.reflection.asm.exceptions.CollectionalPropertyInitializationException;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.utils.StreamUtils;

/**
 * This class provides an API for modifying types at runtime by means of bytecode manipulation.
 * <p>
 * To use this API start with {@link #startModification()}, then perform any other modifications, and end with {@link #endModification()}
 * which loads the modified type and returns a corresponding {@link Class}.
 * <p>
 * <i>Notes on specific parts of the API</i>:
 * <p>
 * If {@link #modifyTypeName(String)} is used, then it must preceed calls to {@link #addProperties(NewProperty...)} and 
 * {@link #modifyProperties(NewProperty...)}. Otherwise an exception is thrown. In case the generated type name is not explicitly modified
 * a unique name is chosen, prefixed by the original type's name.
 * 
 * @param <T> The original type, on which the modified type is based on.
 * 
 * @author TG Team
 *
 */
public class TypeMaker<T> {

    private static final Generated GENERATED_ANNOTATION = new Generated() {
        @Override
        public Class<Generated> annotationType() {
            return Generated.class;
        }
    };

    private static final String CURRENT_BUILDER_IS_NOT_SPECIFIED = "Current builder is not specified.";
    public static final String GET_ORIG_TYPE_METHOD_NAME = "_GET_ORIG_TYPE_METHOD_";

    private final DynamicEntityClassLoader cl;
    private final Class<T> origType;
    private DynamicType.Builder<T> builder;
    private String modifiedName;
    private List<Field> origTypeDeclaredProperties; // lazy access
    private List<Field> origTypeProperties; // lazy access
    private List<Pair<String, Object>> propertyInitializers = new ArrayList<>();
    private Set<String> addedPropertiesNames = new HashSet<>();

    public TypeMaker(final DynamicEntityClassLoader loader, final Class<T> origType) {
        this.cl = loader;
        this.origType = origType;
    }

    /**
     * Initiates adaptation of the specified by name type. This could be either dynamic or static type (created manually by developer).
     *
     * @param typeName
     * @return
     * @throws ClassNotFoundException
     */
    public TypeMaker<T> startModification() throws ClassNotFoundException {
        if (skipAdaptation(origType.getName())) {
            throw new IllegalArgumentException("Java system classes should not be enhanced.");
        }
        // no need for looking up the specified type in cache,
        // which was useful before, since ASM operates on byte[] directly

        builder = new ByteBuddy().subclass(origType)
                // grab all declared annotations
                .annotateType(origType.getDeclaredAnnotations());

        return this;
    }

    /**
     * Enhances currently modified type by adding the specified properties. 
     * If any of the specified properties conflicts with an existing one (e.g. has the same name), then it is discarded.
     * If {@code properties} contains duplicates, then they are discarded to obtain only distinct properties.
     * <p>
     * Added properties are additionally annotated with {@link Generated}.
     *
     * @param properties properties to be added
     * @return this instance to continue building
     */
    public TypeMaker<T> addProperties(final List<NewProperty<?>> properties) {
        if (builder == null) {
            throw new IllegalStateException(CURRENT_BUILDER_IS_NOT_SPECIFIED);
        }

        if (properties == null || properties.isEmpty()) {
            return this;
        }

        final HashSet<String> existingPropNames = getOrigTypeDeclaredProperties().stream()
                .map(Field::getName)
                .collect(toCollection(HashSet::new));

        StreamUtils.distinct(
                properties.stream().filter(prop -> !addedPropertiesNames.contains(prop.getName()) &&
                                                   !existingPropNames.contains(prop.getName())), 
                prop -> prop.getName()) // distinguish properties by name
            .forEach(this::addProperty);

        return this;
    }

    /**
     * Enhances currently modified type by adding the specified properties. 
     * If any of the specified properties conflicts with an existing one (e.g. has the same name), then it is discarded.
     * If {@code properties} contains duplicates, then they are discarded to obtain only distinct properties.
     * <p>
     * Added properties are additionally annotated with {@link Generated}.
     *
     * @param properties properties to be added
     * @return this instance to continue building
     */
    public TypeMaker<T> addProperties(final NewProperty<?>... properties) {
        return addProperties(Arrays.asList(properties));
    }

    private void addProperty(final NewProperty<?> prop) {
        final Type genericType = prop.genericType();
        builder = builder.defineField(prop.getName(), genericType, Visibility.PRIVATE)
                // annotations
                .annotateField(prop.getAnnotations())
                // Generated annotation might already be present
                .annotateField(prop.containsAnnotationDescriptorFor(GENERATED_ANNOTATION.annotationType()) ? 
                               List.of() :
                               List.of(GENERATED_ANNOTATION));

        final boolean collectional = prop.isCollectional();
        if (prop.isInitialized()) {
            propertyInitializers.add(Pair.pair(prop.getName(), prop.getValue()));
        }
        else if (collectional) { // automatically initialize collectional properties
            final Object initValue;
            try {
                initValue = collectionalInitValue(prop.getRawType());
            } catch (Exception e) {
                throw new CollectionalPropertyInitializationException(
                        String.format("Failed to initialize new property %s", prop.toString(IsProperty.class)),
                        e);
            }
            propertyInitializers.add(Pair.pair(prop.getName(), initValue));
        }

        addAccessor(prop.getName(), genericType);
        addSetter(prop.getName(), genericType, collectional);
        
        addedPropertiesNames.add(prop.getName());
    }

    private Object collectionalInitValue(final Class<?> rawType) throws Exception {
        if (rawType == Collection.class || rawType == List.class) {
            return new ArrayList<Object>();
        }
        else if (rawType == Set.class) {
            return new HashSet<Object>();
        }
        else {
            // look for an accessible default constructor
            return rawType.getConstructor().newInstance();
        }
    }

    private void addAccessor(final String propName, final Type propType) {
        // generated type name needs to be known to use low-level ASM
        if (modifiedName == null) {
            modifyTypeName();
        }
        builder = builder.visit(AddPropertyAccessorAdapter.wrapper(propName, propType, modifiedName.replace('.', '/')));
    }

    private void addSetter(final String propName, final Type propType, final boolean collectional) {
        final var building = builder.defineMethod(Mutator.SETTER.startsWith + StringUtils.capitalize(propName), TargetType.DESCRIPTION, Visibility.PUBLIC)
                .withParameter(propType, propName, ParameterManifestation.FINAL);

        final ReceiverTypeDefinition<T> building1;
        if (collectional) {
            // collectional setters are implemented as:
            /*
             this.${propName}.clear();
             this.${propName}.addAll(${arg});
             return this;
             */
            building1 = building.intercept(MethodDelegation.to(new CollectionalSetterInterceptor(propName)).andThen(FixedValue.self()));
        }
        else {
            // regular setters:
            /* 
             this.${propName} = ${arg};  
             return this;
             */
            building1 = building.intercept(FieldAccessor.ofField(propName).setsArgumentAt(0).andThen(FixedValue.self()));
        }

        builder = building1.annotateMethod(ObservableAnnotation.newInstance());
    }

    public static class CollectionalSetterInterceptor {
        private final String propName;

        public CollectionalSetterInterceptor(final String propName) {
            this.propName = propName;
        }

        public void intercept(@This final Object instrumentedInstance, @Argument(0) final Object arg) throws Exception {
            final Field prop = Finder.getFieldByName(instrumentedInstance.getClass(), propName);
            final Object propValue = Finder.getFieldValue(prop, instrumentedInstance);
            // this.${propName}.clear();
            Reflector.getMethod(prop.getType(), "clear").invoke(propValue);
            // this.${propname}.addAll(${arg}): 
            Reflector.getMethod(prop.getType(), "addAll", Collection.class).invoke(propValue, arg);
        }
    }
    
    /**
     * Adds the specified class level annotation to the class. Existing annotations are not replaced.
     * <p>
     * It is important that these annotation have their target specified as <code>TYPE</code> and retention as <code>RUNTIME</code>. 
     * Otherwise, a runtime exception is thrown.
     *
     * @param annotations
     * @return this instance to continue building
     */
    public TypeMaker<T> addClassAnnotations(final Annotation... annotations) {
        if (builder == null) {
            throw new IllegalStateException(CURRENT_BUILDER_IS_NOT_SPECIFIED);
        }

        if (annotations == null || annotations.length == 0) {
            return this;
        }

        final HashSet<String> existingAnnotationNames = Arrays.stream(origType.getDeclaredAnnotations())
                .map(annot -> annot.annotationType().getName())
                .collect(Collectors.toCollection(() -> new HashSet<String>()));
        final List<Annotation> annotationsToAdd = Arrays.stream(annotations)
                .filter(annot -> !existingAnnotationNames.contains(annot.annotationType().getName()))
                .toList();

        // let's validate provided annotations
        annotationsToAdd.forEach(annot -> {
            // check retention policy
            final Retention retention = annot.annotationType().getAnnotation(Retention.class);
            if (retention == null || retention.value() != RetentionPolicy.RUNTIME) {
                throw new IllegalArgumentException(format("The provided annotation %s should have runtime retention policy.",
                        annot.annotationType().getSimpleName()));
            }

            // check target
            final Target target = annot.annotationType().getAnnotation(Target.class);
            if (target == null || !Arrays.stream(target.value()).anyMatch(t -> t == ElementType.TYPE)) {
                throw new IllegalArgumentException(format("The provided annotation %s should have 'type' target.",
                        annot.annotationType().getSimpleName()));
            }          
        });

        // proceed with type construction
        builder = builder.annotateType(annotationsToAdd);
        return this;
    }

    /**
     * Modifies type's name with the specified <code>newTypeName</code>. 
     * 
     * @param newTypeName - must be fully-qualified in a binary format (e.g. <code>foo.Bar</code> )
     * @return
     */
    public TypeMaker<T> modifyTypeName(final String newTypeName) {
        if (modifiedName != null) {
            throw new IllegalStateException("Type name can't be modified past this point.");
        }
        if (StringUtils.isEmpty(newTypeName)) {
            throw new IllegalStateException("New type name is 'null' or empty.");
        }
        if (builder == null) {
            throw new IllegalStateException(CURRENT_BUILDER_IS_NOT_SPECIFIED);
        }
        modifiedName = newTypeName;
        builder = builder.name(newTypeName);
        return this;
    }
    
    private String modifyTypeName() {
        final String newName = DynamicTypeNamingService.nextTypeName(origType.getName());
        modifyTypeName(newName);
        return newName;
    }

    /**
     * Enhances currently modified type by modifying properties that exist in its original type.
     * <p>
     * If {@code propertyReplacements} contains multiple properties with the same name, then only one of these will be considered.
     * <p>
     * Modifying a property with the same name in multiple sequential calls is illegal. That is, a property can be modified only once.
     * Modifying a property that previously added with {@link #addProperties(List)} is also illegal for the same reasons.
     *
     * @param propertyReplacements
     * @return this instance to continue building
     */
    public TypeMaker<T> modifyProperties(final List<NewProperty<?>> propertyReplacements) throws IllegalArgumentException {
        if (builder == null) {
            throw new IllegalStateException(CURRENT_BUILDER_IS_NOT_SPECIFIED);
        }

        if (propertyReplacements == null || propertyReplacements.isEmpty()) {
            return this;
        }

        // distinguish properties by name
        final List<NewProperty<?>> distinctPropertyReplacements = StreamUtils.distinct(propertyReplacements.stream(),
                prop -> prop.getName()).toList();
        
        // modifying a property that doesn't exist in the original type's hierarchy is illegal
        final List<String> existingPropNames = getOrigTypeProperties().stream().map(Field::getName).toList();
        final NewProperty<?> nonExistentNp = distinctPropertyReplacements.stream()
                .filter(prop -> !existingPropNames.contains(prop.getName()))
                .findAny().orElse(null);
        if (nonExistentNp != null) {
            throw new IllegalArgumentException("Unable to modify property \"%s\" that does not belong to the original type."
                    .formatted(nonExistentNp.getName()));
        }

        // modifying the same property multiple times is illegal
        final NewProperty<?> illegalNp = distinctPropertyReplacements.stream()
            .filter(prop -> addedPropertiesNames.contains(prop.getName()))
            .findAny().orElse(null);
        if (illegalNp != null) {
            throw new IllegalArgumentException("Property \"%s\" was already added or modified for this type."
                    .formatted(illegalNp.toString(true)));
        }

        distinctPropertyReplacements.stream()
            .map(prop -> prop.deprecated ? undeprecateProperty(prop) : prop)
            .forEach(this::addProperty);

        return this;
    }

    /**
     * Enhances currently modified type by modifying existing properties with the specified ones.
     * The same rules apply as for {@link #addProperties(List)}.
     *
     * @param propertyReplacements
     * @return this instance to continue building
     */
    public TypeMaker<T> modifyProperties(final NewProperty<?>... propertyReplacements) {
        return modifyProperties(Arrays.asList(propertyReplacements));
    }

    private NewProperty<?> undeprecateProperty(final NewProperty<?> property) {
        // operates on deprecated NewProperty; only name and raw type information
        final Field origField = Finder.findFieldByName(origType, property.getName());

        if (property.changeSignature) {
            // NewProperty.type represents a type argument
            return NewProperty.fromField(origField).setTypeArguments(property.getRawType());
        }
        return NewProperty.fromField(origField).changeType(property.getRawType());
    }

    /**
     * Generates code to capture the original type, which is provided via <code>type</code>.
     * @param type - the type to be recorded
     */
    private void recordOrigType(final Class<?> type) {
        builder = builder.defineMethod(GET_ORIG_TYPE_METHOD_NAME, type.getClass(), Visibility.PUBLIC, Ownership.STATIC)
                .intercept(FixedValue.value(type));
    }

    /**
     * Finalizes type modification and loads the resulting class.
     * <p>
     * If the type name wasn't modified prior to this stage, then it is performed here according to {@link DynamicTypeNamingService}.
     * 
     * @return a loaded class representing the modified type
     */
    public Class<? extends T> endModification() {
        if (builder == null) {
            throw new IllegalStateException(CURRENT_BUILDER_IS_NOT_SPECIFIED);
        }

        if (!DynamicEntityClassLoader.isGenerated(origType)) {
            recordOrigType(origType);
        }

        if (modifiedName == null) {
            modifyTypeName();
        }

        if (!propertyInitializers.isEmpty()) {
            // initialize all fields in this.propertyInitializers by intercepting the default constructor
            // which calls its parent constructor and then does the initialization
            builder = builder.constructor(ElementMatchers.isDefaultConstructor())
                    .intercept(SuperMethodCall.INSTANCE.andThen(MethodDelegation.to(new ConstructorInterceptor(propertyInitializers))));
        }
        
        // provide a TypePool that uses the class loader of the original type
        // if origType is a dynamic one, then this will be DynamicEntityClassLoader, which will be able to locate origType
        return builder.make(TypePool.ClassLoading.of(origType.getClassLoader()))
                // provide DynamicEntityClassLoader to be injected with the new dynamic type
                // this allows us to use a single class loader for all dynamically created types,
                // instead of making ByteBuddy create a separate class loader for each
                .load(cl)
                .getLoaded();
    }

    public static class ConstructorInterceptor {
        private final List<Pair<String, Object>> fieldInitializers = new ArrayList<>();

        ConstructorInterceptor(final List<Pair<String, Object>> fieldInitializers) {
            if (fieldInitializers != null) {
                this.fieldInitializers.addAll(fieldInitializers);
            }
        }

        public void intercept(@This final Object instrumentedInstance) throws Exception {
            for (final Pair<String, Object> nameAndValue : fieldInitializers) {
                final Field prop = Finder.getFieldByName(instrumentedInstance.getClass(), nameAndValue.getKey());
                final boolean accessible = prop.canAccess(instrumentedInstance);
                prop.setAccessible(true);
                prop.set(instrumentedInstance, nameAndValue.getValue());
                prop.setAccessible(accessible);
            }
        }
    }

    private boolean skipAdaptation(final String name) {
        return name.startsWith("java.");
    }

    private List<Field> getOrigTypeDeclaredProperties() {
        if (origTypeDeclaredProperties == null) {
            origTypeDeclaredProperties = Arrays.stream(origType.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(IsProperty.class))
                    .toList(); 
        }
        return origTypeDeclaredProperties;
    }
    
    private List<Field> getOrigTypeProperties() {
        if (origTypeProperties == null) {
            origTypeProperties = Finder.findProperties(origType); 
        }
        return origTypeProperties;
    }

}
