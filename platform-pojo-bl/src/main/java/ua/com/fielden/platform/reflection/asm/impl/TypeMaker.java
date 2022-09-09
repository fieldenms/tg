package ua.com.fielden.platform.reflection.asm.impl;

import static java.lang.String.format;
import static java.util.stream.Collectors.toCollection;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
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
import ua.com.fielden.platform.entity.Accessor;
import ua.com.fielden.platform.entity.Mutator;
import ua.com.fielden.platform.entity.annotation.Generated;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.factory.ObservableAnnotation;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
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
 * If {@link #modifyTypeName(String)} is not called, then {@link #endModification()} will most likely fail due to a name conflict 
 * with the original type.
 * <p>
 * {@link #modifyTypeName(String)} should be called, if needed, <b>only after all other modifications</b>, in order to guarantee
 * correct renaming of all occurences of the previous type name.
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
    private static final String NEW_SUPERTYPE_NAME_IS_NULL_OR_EMPTY = "New supertype name is 'null' or empty.";
    private static final String CURRENT_BUILDER_IS_NOT_SPECIFIED = "Current builder is not specified.";
    public static final String GET_ORIG_TYPE_METHOD_NAME = "_GET_ORIG_TYPE_METHOD_";
    private final DynamicEntityClassLoader cl;
    private final Class<T> origType;
    private DynamicType.Builder<T> builder;
    private boolean nameModified = false;
    private List<Field> origTypeDeclaredProperties; // lazy access
    private List<Pair<String, Object>> propertyInitializers = new ArrayList<>();

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
     * Adds the specified properties to the type. 
     * Those properties that conflict with the existing ones are discarded (i.e. old properties are not overwritten).
     * Also, duplicate properties are eliminated.
     * <p>
     * <i>Note:</i> Some annotations, such as {@link Title} and {@link Generated}, are explicitly added for each property. Therefore, elements of <code>properties</code> should not contain these annotations in {@link NewProperty#annotations}.
     *
     * @param properties
     * @return
     */
    public TypeMaker<T> addProperties(final NewProperty... properties) {
        if (builder == null) {
            throw new IllegalStateException(CURRENT_BUILDER_IS_NOT_SPECIFIED);
        }

        if (properties == null || properties.length == 0) {
            return this;
        }

        final HashSet<String> existingPropNames = getOrigTypeDeclaredProperties().stream()
                .map(Field::getName)
                .collect(toCollection(HashSet::new));

        StreamUtils.distinct(
                Arrays.stream(properties).filter(prop -> !existingPropNames.contains(prop.getName())), 
                prop -> prop.getName()) // distinguish properties by name
            .forEach(this::addProperty);

        return this;
    }

    private void addProperty(final NewProperty prop) {
        final Type genericType = prop.genericType();
        builder = builder.defineField(prop.getName(), genericType, Visibility.PRIVATE)
                // annotations
                .annotateField(prop.getAnnotations())
                // Generated annotation might already be present
                .annotateField(prop.containsAnnotationDescriptorFor(GENERATED_ANNOTATION.annotationType()) ? 
                               List.of(prop.titleAnnotation()) :
                               List.of(prop.titleAnnotation(), GENERATED_ANNOTATION));

        final boolean collectional = prop.isCollectional();
        // try to determine an initializer for a collectional property
        if (collectional) {
            final Object initValue;
            try {
                initValue = collectionalInitValue(prop.getRawType());
            } catch (Exception e) {
                throw new CollectionalPropertyInitializationException(
                        String.format("Failed to initialize new property %s", prop.toString(IsProperty.class)),
                        e);
            }
            if (initValue != null) {
                propertyInitializers.add(Pair.pair(prop.getName(), initValue));
            }
        }

        addGetter(prop.getName(), genericType);
        addSetter(prop.getName(), genericType, collectional);
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

    private void addGetter(final String propName, final Type propType) {
        final String prefix = propType.equals(Boolean.class) ? Accessor.IS.startsWith : Accessor.GET.startsWith;
        builder = builder.defineMethod(prefix + StringUtils.capitalize(propName), propType, Visibility.PUBLIC)
                .intercept(FieldAccessor.ofField(propName));
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
     * @return
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
        if (StringUtils.isEmpty(newTypeName)) {
            throw new IllegalStateException("New type name is 'null' or empty.");
        }
        if (builder == null) {
            throw new IllegalStateException(CURRENT_BUILDER_IS_NOT_SPECIFIED);
        }
        nameModified = true;
        builder = builder.name(newTypeName);
        return this;
    }

    /**
     * Modifies the supertype's name with the specified <code>newSupertypeName</code>. 
     * <p>
     * 
     * @param newSupertypeName - must be fully-qualified in a binary format 
     * (e.g. <code>foo.Bar</code> )
     * @return
     */
    public TypeMaker<T> modifySupertypeName(final String newSupertypeName) {
        if (StringUtils.isEmpty(newSupertypeName)) {
            throw new IllegalStateException(NEW_SUPERTYPE_NAME_IS_NULL_OR_EMPTY);
        }
        if (builder == null) {
            throw new IllegalStateException(CURRENT_BUILDER_IS_NOT_SPECIFIED);
        }
        // DynamicType.Builder does not provide supertype modification capabilities
        // so we have to use an ASM wrapper
        builder = builder.visit(AdvancedChangeSupertypeAdapter.asAsmVisitorWrapper(newSupertypeName));
        return this;
    }

    /**
     * Sets the supertype's name to the name of the original type.
     * A shortcut for {@link #modifySupertypeName(String)} where the argument is the original type's name.
     * @return
     */
    public TypeMaker<T> extendOriginalType() {
        modifySupertypeName(origType.getName());
        return this;
    }

    /**
     * Modifies type's properties with the specified properties.
     *
     * @param propertyReplacements
     * @return
     */
    public TypeMaker<T> modifyProperties(final NewProperty... propertyReplacements) {
        if (builder == null) {
            throw new IllegalStateException(CURRENT_BUILDER_IS_NOT_SPECIFIED);
        }

        if (propertyReplacements == null || propertyReplacements.length == 0) {
            return this;
        }

        StreamUtils.distinct(Arrays.stream(propertyReplacements), prop -> prop.getName()) // distinguish properties by name
            .map(prop -> prop.deprecated ? modifyProperty(prop) : prop)
            .forEach(this::addProperty);

        return this;
    }

    private NewProperty modifyProperty(final NewProperty property) {
        // operates on deprecated NewProperty; only name and raw type information
        final Field origField = Finder.findFieldByName(origType, property.getName());

        if (property.changeSignature) {
            // NewProperty.type represents a type argument
            return NewProperty.fromField(origField).setType(new ParameterizedType() {
                @Override
                public Type[] getActualTypeArguments() { return new Type[] { property.getRawType() }; }
                @Override
                public Type getRawType() { return origField.getType(); }
                @Override
                public Type getOwnerType() { return origField.getType().getDeclaringClass(); }
            });
        }
        return NewProperty.fromField(origField).setRawType(property.getRawType());
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
        if (!DynamicEntityClassLoader.isGenerated(origType)) {
            recordOrigType(origType);
        }

        if (!nameModified) {
            modifyTypeName(DynamicTypeNamingService.nextTypeName(origType.getName()));
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
            origTypeDeclaredProperties = Arrays.stream(origType.getDeclaredFields()).filter(field -> field.isAnnotationPresent(IsProperty.class)).toList(); 
        }
        return origTypeDeclaredProperties;
    }

}
