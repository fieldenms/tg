package ua.com.fielden.platform.reflection.asm.impl;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toCollection;
import static ua.com.fielden.platform.cypher.Checksum.sha256;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedSetOf;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.ParameterManifestation;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition;
import net.bytebuddy.dynamic.TargetType;
import net.bytebuddy.dynamic.scaffold.MethodGraph;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.pool.TypePool;
import ua.com.fielden.platform.entity.Accessor;
import ua.com.fielden.platform.entity.Mutator;
import ua.com.fielden.platform.entity.annotation.Generated;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.factory.ObservableAnnotation;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.asm.annotation.GeneratedAnnotation;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.reflection.asm.exceptions.CollectionalPropertyInitializationException;
import ua.com.fielden.platform.reflection.asm.exceptions.TypeMakerException;
import ua.com.fielden.platform.types.tuples.T2;

/**
 * This class provides an API for modifying types at runtime by means of bytecode manipulation.
 * <p>
 * To use this API start with {@link #startModification()}, then perform any other modifications, and end with {@link #endModification()}
 * which loads the modified type and returns a corresponding {@link Class}.
 * <p>
 * <i>Notes on implicit modifications performed by the API:</i>
 * <ul>
 *   <li>
 *   Generated types will have an additional method generated for accessing the original type, unless the original type itself 
 *   is a generated type, in which case the mentioned method will be inherited by the resultant generated type.
 *   The mentioned method is named as defined by {@link #GET_ORIG_TYPE_METHOD_NAME} constant.
 *   </li>
 *   <li>
 *   In case a generated type's name is not explicitly set with {@link #modifyTypeName(String)}, a unique name is generated, prefixed
 *   by the original type's name.
 *   </li>
 * </ul>
 * <i>Notes on using the API:</i>
 * <ul>
 *   <li>
 *   Certain classes, such as those that are a part of the Java platform, cannot be modified. 
 *   For details refer to the implementation of {@link #skipAdaptation(String)}.
 *   </li>
 * </ul>
 * 
 * @param <T> The original type, on which the modified type is based on.
 * 
 * @author TG Team
 *
 */
public class TypeMaker<T> {

    private static final Generated GENERATED_ANNOTATION = GeneratedAnnotation.newInstance();
    private static final String CURRENT_BUILDER_IS_NOT_SPECIFIED = "Current builder is not specified.";
    public static final String GET_ORIG_TYPE_METHOD_NAME = "_GET_ORIG_TYPE_METHOD_";
    private static final String ERR_FAILED_TO_INITIALISE_COLLECTIONAL_PROPERTY = "Failed to initialise new collectional property [%s].";
    private static final String ERR_FAILED_TO_INITIALISE_CUSTOM_COLLECTIONAL_PROPERTY = "Failed to initialise new collectional property of custom type [%s].";
    private static final String CONSTRUCTOR_FIELD_PREFIX = "constructorInterceptor$";
    private static final String COLLECTIONAL_SETTER_FIELD_PREFIX = "collectionalSetterInterceptor$";

    private final DynamicEntityClassLoader cl;
    private final Class<T> origType;
    private DynamicType.Builder<T> builder;
    private String modifiedName;

    /**
     * Enables lazy access to all (declared + inherited) properties of the original type.
     */
    private final Set<String> origTypeProperties;// = new LinkedHashSet<>();
    /**
     * Holds mappings of the form: {@code property name -> initialized value supplier}.
     */
    private final Map<String, Supplier<?>> propertyInitializers = new HashMap<>();
    /**
     * Storage for names of both added and modified properties and corresponding [NewProperty, prop Type] pairs.
     */
    private final Map<String, T2<NewProperty<?>, Type>> addedProperties = new LinkedHashMap<>();

    public TypeMaker(final DynamicEntityClassLoader loader, final Class<T> origType) {
        this.cl = loader;
        this.origType = origType;
        this.origTypeProperties = Finder.streamProperties(origType, IsProperty.class)
                                        .map(Field::getName)
                                        .collect(toCollection(LinkedHashSet::new));
    }

    /**
     * Initiates adaptation of the specified by name type. This could be either dynamic or static type (created manually by developer).
     *
     * @return
     * @throws ClassNotFoundException
     */
    public TypeMaker<T> startModification() throws ClassNotFoundException {
        if (skipAdaptation(origType.getName())) {
            throw new TypeMakerException("Java system classes should not be enhanced.");
        }
        // no need for looking up the specified type in cache,
        // which was useful before, since ASM operates on byte[] directly

        builder = new ByteBuddy()
                // By JVM rules 2 methods are considered different if their parameter and return types are different
                // in contrast to Java rules, where only parameter types are considered (default choice of ByteBuddy instances).
                // JVM rules enable us to generate methods with modified return type, while avoiding implicit generation
                // of bridge methods by Java compiler. This is the case for generation of subclasses.
                .with(MethodGraph.Compiler.Default.forJVMHierarchy())
                .subclass(origType, 
                          // do not implicitly define any constructors, since this is done manually later
                          ConstructorStrategy.Default.NO_CONSTRUCTORS)
                // grab all declared class-level annotations
                .annotateType(origType.getDeclaredAnnotations());

        return this;
    }

    /**
     * Enhances currently modified type by adding the specified properties. 
     * If any of the specified properties conflicts with an existing one (e.i., has the same name), then it is ignored.
     * <p>
     * Added properties are annotated with {@link Generated}.
     *
     * @param properties to be added
     * @return this instance to continue building
     */
    public TypeMaker<T> addProperties(final Set<NewProperty<?>> properties) {
        if (builder == null) {
            throw new TypeMakerException(CURRENT_BUILDER_IS_NOT_SPECIFIED);
        }

        if (properties == null || properties.isEmpty()) {
            return this;
        }

        properties.stream()
        .filter(prop -> !addedProperties.containsKey(prop.getName()) && 
                        !origTypeProperties.contains(prop.getName())) 
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
        return addProperties(linkedSetOf(properties));
    }

    private void addProperty(final NewProperty<?> prop) {
        final Type genericType = prop.genericType();
        builder = builder.defineField(prop.getName(), genericType, Visibility.PRIVATE)
                // annotations
                .annotateField(prop.getAnnotations())
                // annotation @Generated might already be present
                .annotateField(prop.containsAnnotationDescriptorFor(GENERATED_ANNOTATION.annotationType()) 
                               ? List.of()
                               : List.of(GENERATED_ANNOTATION));

        if (prop.isInitialised()) {
            // it is guaranteed at this point that `prop` hasn't been put into the map previously
            // since properties with duplicate names are not allowed
            propertyInitializers.put(prop.getName(), prop.getValueSupplier());
        }
        else if (prop.isCollectional()) { // automatically initialize collectional properties
            try {
                propertyInitializers.put(prop.getName(), collectionalInitValueSupplier(prop.getRawType()));
            } catch (final Exception ex) {
                throw new CollectionalPropertyInitializationException(ERR_FAILED_TO_INITIALISE_COLLECTIONAL_PROPERTY.formatted(prop.toString()), ex);
            }
        }

        addAccessor(prop.getName(), genericType);
        // delay addSetter(...) to .endModification() stage

        addedProperties.put(prop.getName(), t2(prop, genericType));
    }

    /**
     * Returns a fitting value supplier to initialise an instance of collectional type {@code rawType}.
     *
     * @param rawType
     * @return
     * @throws Exception
     */
    private Supplier<Object> collectionalInitValueSupplier(final Class<?> rawType) {
        if (rawType == Collection.class || rawType == List.class) {
            return ArrayList::new;
        }
        else if (rawType == Set.class) {
            return HashSet::new;
        }
        else {
            customCollectionalInitValue(rawType); // perform early check for ability to create custom collection at the level of TypeMaker.addProperties(...) to preserve context
            return () -> customCollectionalInitValue(rawType); // this function will be computed during TypeMaker.endModification() phase
        }
    }

    /**
     * Tries to compute empty default collectional property value for custom {@code rawType} using default parameterless constructor.
     */
    private Object customCollectionalInitValue(final Class<?> rawType) {
        try {
            // look for an accessible default constructor
            return rawType.getConstructor().newInstance();
        } catch (final Exception ex) {
            throw new CollectionalPropertyInitializationException(ERR_FAILED_TO_INITIALISE_CUSTOM_COLLECTIONAL_PROPERTY.formatted(rawType), ex);
        }
    }

    private void addAccessor(final String propName, final Type propType) {
        final String prefix = propType.equals(Boolean.class) || propType.equals(boolean.class) ? Accessor.IS.startsWith : Accessor.GET.startsWith;
        builder = builder.defineMethod(prefix + StringUtils.capitalize(propName), propType, Visibility.PUBLIC)
                         .intercept(FieldAccessor.ofField(propName));
    }

    private void addSetter(final String propName, final Type propType, final boolean collectional, final String modifiedTypeName) {
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
            building1 = building.intercept(MethodDelegation.to(new CollectionalSetterInterceptor(propName), COLLECTIONAL_SETTER_FIELD_PREFIX + sha256(modifiedTypeName.getBytes(UTF_8)) + propName).andThen(FixedValue.self()));
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
            throw new TypeMakerException(CURRENT_BUILDER_IS_NOT_SPECIFIED);
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
                throw new TypeMakerException("The provided annotation %s should have runtime retention policy.".formatted(annot.annotationType().getSimpleName()));
            }

            // check target
            final Target target = annot.annotationType().getAnnotation(Target.class);
            if (target == null || !Arrays.stream(target.value()).anyMatch(t -> t == ElementType.TYPE)) {
                throw new TypeMakerException("The provided annotation %s should have 'type' target.".formatted(annot.annotationType().getSimpleName()));
            }          
        });

        // proceed with type construction
        builder = builder.annotateType(annotationsToAdd);
        return this;
    }

    /**
     * Modifies type's name with the specified {@code newTypeName}. 
     * 
     * @param newTypeName - must be fully-qualified in a binary format (e.g., {@code foo.Bar}).
     * @return
     */
    public TypeMaker<T> modifyTypeName(final String newTypeName) {
        if (StringUtils.isBlank(newTypeName)) {
            throw new TypeMakerException("New type name cannot be blank.");
        }
        if (builder == null) {
            throw new TypeMakerException(CURRENT_BUILDER_IS_NOT_SPECIFIED);
        }
        builder = builder.name(newTypeName);
        modifiedName = newTypeName;
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
     * Modifying a property that previously added with {@link #addProperties(Set)} is also illegal for the same reasons.
     *
     * @param propertyReplacements
     * @return this instance to continue building
     */
    public TypeMaker<T> modifyProperties(final Set<NewProperty<?>> propertyReplacements) throws IllegalArgumentException {
        if (builder == null) {
            throw new TypeMakerException(CURRENT_BUILDER_IS_NOT_SPECIFIED);
        }

        if (propertyReplacements == null || propertyReplacements.isEmpty()) {
            return this;
        }

        // modifying a property that doesn't exist in the original type's hierarchy is illegal
        final Optional<NewProperty<?>> nonExistentNp = propertyReplacements.stream()
                .filter(prop -> !origTypeProperties.contains(prop.getName()))
                .findAny();
        if (nonExistentNp.isPresent()) {
            throw new TypeMakerException("Unable to modify property [%s] that does not belong to the original type.".formatted(nonExistentNp.get().getName()));
        }

        // modifying the same property multiple times is illegal
        final NewProperty<?> illegalNp = propertyReplacements.stream()
            .filter(prop -> addedProperties.containsKey(prop.getName()))
            .findAny().orElse(null);
        if (illegalNp != null) {
            throw new TypeMakerException("Property [%s] was already added or modified for this type.".formatted(illegalNp.toString(true)));
        }

        propertyReplacements.stream().forEach(this::addProperty);

        return this;
    }

    /**
     * Enhances currently modified type by modifying existing properties with the specified ones.
     * The same rules apply as for {@link #addProperties(Set)}.
     *
     * @param propertyReplacements
     * @return this instance to continue building
     */
    public TypeMaker<T> modifyProperties(final NewProperty<?>... propertyReplacements) {
        return modifyProperties(linkedSetOf(propertyReplacements));
    }

    /**
     * Generates code to capture the original type, which is provided via <code>type</code>.
     * @param type - the type to be recorded
     */
    private void recordOrigType(final Class<?> type) {
        builder = builder.defineMethod(GET_ORIG_TYPE_METHOD_NAME, type.getClass(), Visibility.PUBLIC, Ownership.STATIC)
                .intercept(FixedValue.value(type));
    }

    private void generateConstructors(final String modifiedTypeName) {
        final Implementation impl = propertyInitializers.isEmpty() 
                                    ? SuperMethodCall.INSTANCE
                                    : SuperMethodCall.INSTANCE.andThen(MethodDelegation.to(new ConstructorInterceptor(propertyInitializers), CONSTRUCTOR_FIELD_PREFIX + sha256(modifiedTypeName.getBytes(UTF_8))));

        final List<Constructor<?>> visibleConstructors = Arrays.stream(origType.getDeclaredConstructors())
                .filter(constr -> !Modifier.isPrivate(constr.getModifiers()))
                .toList();

        for (final Constructor<?> origConstr: visibleConstructors) {
            // manually define a constructor to imitate the original type's constructor
            MethodDefinition<T> methodBuilder = builder.defineConstructor(origConstr.getModifiers())
                    .withParameters(origConstr.getGenericParameterTypes())
                    .throwing(origConstr.getGenericExceptionTypes())
                    .intercept(impl)
                    // constructor-level annotations
                    .annotateMethod(origConstr.getDeclaredAnnotations());

            // constructor parameter-level annotations
            final Annotation[][] paramAnnotations = origConstr.getParameterAnnotations();
            for (int i = 0; i < paramAnnotations.length; i++) {
                final Annotation[] annotations = paramAnnotations[i];
                if (annotations.length > 0) {
                    methodBuilder = methodBuilder.annotateParameter(i, annotations);
                }
            }

            builder = methodBuilder;
        }
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
            throw new TypeMakerException(CURRENT_BUILDER_IS_NOT_SPECIFIED);
        }

        if (!DynamicEntityClassLoader.isGenerated(origType)) {
            recordOrigType(origType);
        }

        if (modifiedName == null) {
            modifyTypeName();
        }

        // delay adding constructors and setters to the stage where 'modifiedName' is already known;
        // MethodDelegation static fields will be named exactly the same in identical types to facilitate proper concurrent generation
        // see TypeResolutionStrategy.Passive.initialize method with onLoad(...) invocations after classLoadingStrategy.load(...) may have returned already loaded / cached types from other concurrent thread
        generateConstructors(modifiedName);
        addedProperties.values().forEach(propAndType -> addSetter(propAndType._1.getName(), propAndType._2, propAndType._1.isCollectional(), modifiedName));

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
        private final Map<String, Supplier<?>> fieldInitializers = new HashMap<>();

        ConstructorInterceptor(final Map<String, Supplier<?>> fieldInitializers) {
            if (fieldInitializers != null) {
                this.fieldInitializers.putAll(fieldInitializers);
            }
        }

        public void intercept(@This final Object instrumentedInstance) throws Exception {
            for (final Entry<String, Supplier<?>> nameAndValue : fieldInitializers.entrySet()) {
                final Field prop = Finder.getFieldByName(instrumentedInstance.getClass(), nameAndValue.getKey());
                final boolean accessible = prop.canAccess(instrumentedInstance);
                prop.setAccessible(true);
                prop.set(instrumentedInstance, nameAndValue.getValue().get()); // supplier should never be null here (see collectionalInitValueSupplier)
                prop.setAccessible(accessible);
            }
        }
    }

    private boolean skipAdaptation(final String name) {
        return name.startsWith("java.");
    }

}