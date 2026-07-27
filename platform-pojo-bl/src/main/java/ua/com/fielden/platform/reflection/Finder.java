package ua.com.fielden.platform.reflection;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Monitoring;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.types.either.Right;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.CollectionUtil;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static ua.com.fielden.platform.entity.AbstractEntity.*;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.commonProperties;
import static ua.com.fielden.platform.entity.annotation.IsProperty.DEFAULT_LINK_PROPERTY;
import static ua.com.fielden.platform.entity.meta.PropertyDescriptor.pd;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.reflection.Reflector.MAXIMUM_CACHE_SIZE;
import static ua.com.fielden.platform.types.try_wrapper.TryWrapper.Try;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.first;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;
import static ua.com.fielden.platform.utils.EntityUtils.*;

/**
 * This is a helper class to provide :
 * <p>
 * 1. Finding logic for properties and fields.
 * <p>
 * 2. Finding logic for MetaProperti'es or PropertyDescriptor's.
 *
 * @author TG Team
 *
 */
public class Finder {
    private static final Cache<Class<?>, List<Field>> ENTITY_KEY_MEMBERS = CacheBuilder.newBuilder().weakKeys().initialCapacity(1000).maximumSize(MAXIMUM_CACHE_SIZE).concurrencyLevel(50).build();

    /// A cache for common properties of union entity types.
    /// * Key: canonical union entity type.
    ///   It is not necessary to record enhanced union types in this cache, as the enhancements, if any, should not affect the set of common properties.
    /// * Value: set of common property names.
    ///
    private static final Cache<Class<?>, SequencedSet<String>> UNION_COMMON_PROPERTIES = CacheBuilder.newBuilder().initialCapacity(50).maximumSize(MAXIMUM_CACHE_SIZE).concurrencyLevel(50).build();

    /// A cache for union member properties.
    /// * Key: canonical union entity type.
    ///   It is not necessary to record enhanced union types in this cache, as the enhancements, if any, should not affect the set of union members.
    /// * Value: list of union member properties.
    ///
    private static final Cache<Class<?>, List<Field>> UNION_MEMBER_PROPERTIES = CacheBuilder.newBuilder().initialCapacity(50).maximumSize(MAXIMUM_CACHE_SIZE).concurrencyLevel(50).build();

    public static long cleanUp() {
        ENTITY_KEY_MEMBERS.cleanUp();
        UNION_COMMON_PROPERTIES.cleanUp();
        UNION_MEMBER_PROPERTIES.cleanUp();
        return ENTITY_KEY_MEMBERS.size() + UNION_COMMON_PROPERTIES.size() + UNION_MEMBER_PROPERTIES.size();
    }

    /**
     * Let's hide default constructor, which is not needed for a static class.
     */
    private Finder() {
    }

    // ======================================================================================================
    ///////////////////////////////////// Finding/getting MetaProperties and PropertyDescriptors ////////////
    /**
     * Produces a list of property descriptors for "real properties" of a given entity type, including properties declared in its type hierarchy.
     * Properties with denied introspection are excluded.
     */
    public static <T extends AbstractEntity<?>> List<PropertyDescriptor<T>> getPropertyDescriptors(final Class<T> entityType) {
        return getPropertyDescriptors(entityType, f -> false);
    }

    /**
     * The same as {@link #getPropertyDescriptors(Class)}, but with the ability to skip some properties.
     * Properties with denied introspection are always excluded.
     */
    public static <T extends AbstractEntity<?>> List<PropertyDescriptor<T>> getPropertyDescriptors(final Class<T> entityType, final Predicate<Field> shouldSkip) {
        return streamRealProperties(entityType)
                .filter(prop -> isIntrospectionAllowed(prop) && !shouldSkip.test(prop))
                .map(f -> pd(entityType, f.getName()))
                .collect(toList());
    }

    /**
     * Does much the same as {@link #findMetaProperty(AbstractEntity, String)}, but it retrieves all {@link MetaProperty}s specified in <code>dotExpr</code>. If, during
     * retrieval, some of the properties is null, then this method returns only that properties, which were retrieved.<br>
     * <br>
     *
     * @param entity
     * @param dotExpr
     * @return
     * @throws RuntimeException
     */
    public static List<MetaProperty<?>> findMetaProperties(final AbstractEntity<?> entity, final String dotExpr) {
        final String[] properties = laxSplitPropPathToArray(dotExpr);
        final List<MetaProperty<?>> metaProperties = new ArrayList<>();
        Object owner = entity;
        for (final String propertyName : properties) {
            // if the owner is null or not an entity then there is no way to determine meta-properties at the next level.
            if (!(owner instanceof AbstractEntity)) {
                break;
            }
            // get the meta-property instance, which can but should not be null
            final Optional<MetaProperty<?>> op = ((AbstractEntity<?>) owner).getPropertyOptionally(propertyName);
            if (op.isPresent()) {
                metaProperties.add(op.get());
            } else {
                throw new ReflectionException("Failed to locate meta-property " + dotExpr + " starting with entity " + entity.getType() + ": " + entity);
            }
            // obtain the value for the current property, which might be an entity instance
            // and needs to be recognized as an owner for the property at the next level
            owner = op.get().getValue();
        }

        return metaProperties;
    }

    /**
     * Looks for an instance of {@link MetaProperty} for a property identified by an expression with dot notation.
     * <p>
     * The first part of the expression should correspond to a property in the provided entity.
     * <p>
     * The last part should correspond to a property for which meta-property is being determined.
     */
    public static MetaProperty<?> findMetaProperty(final AbstractEntity<?> entity, final String dotExpr) {
        final List<MetaProperty<?>> metaProperties = findMetaProperties(entity, dotExpr);
        if (laxSplitPropPathToArray(dotExpr).length > metaProperties.size()) {
            return null;
        } else {
            return metaProperties.getLast();
        }
    }

    /**
     * Obtains a set of meta-properties from an entity, sorted in a natural order as defined by {@link MetaProperty}.
     */
    public static SortedSet<MetaProperty<?>> getMetaProperties(final AbstractEntity<?> entity) {
        final List<Field> properties = findRealProperties(entity.getType());
        final SortedSet<MetaProperty<?>> metaProperties = new TreeSet<>();
        for (final Field property : properties) {
            metaProperties.add(entity.getProperty(property.getName()));
        }
        return metaProperties;
    }

    /**
     * Obtains a list of collectional meta-properties of the specified element type from an entity. The resultant list sorted in a natural order as defined by {@link MetaProperty}.
     *
     * @param entity
     * @param collectionType
     *            -- the type of the collection elements
     * @return
     * @throws RuntimeException
     */
    public static List<MetaProperty<?>> getCollectionalMetaProperties(final AbstractEntity<?> entity, final Class<?> collectionType) {
        final SortedSet<MetaProperty<?>> metaProperties = getMetaProperties(entity);
        final List<MetaProperty<?>> collectional = new ArrayList<>();
        for (final MetaProperty<?> metaProperty : metaProperties) {
            if (metaProperty.isCollectional() && metaProperty.getPropertyAnnotationType() == collectionType) {
                collectional.add(metaProperty);
            }
        }
        return collectional;
    }

    // ========================================================================================================
    /////////////////////////////// Finding properties ////////////////////////////////////////////////////////

    /**
     * Returns properties (fields annotated with {@link IsProperty}) for <code>entityType</code> that are also annotated with specified <code>annotations</code> (if any).
     * <p>
     * Takes into account the fact that <code>entityType</code> might be an {@link AbstractUnionEntity} class (it means that {@link AbstractUnionEntity}'s common properties are
     * included).
     *
     * @param entityType
     * @param annotations
     *
     * @return
     */
    @SafeVarargs
    public static List<Field> findProperties(final Class<?> entityType, final Class<? extends Annotation>... annotations) {
        return streamProperties(entityType, annotations).collect(toList());
    }

    /**
     * A stream equivalent to method {@link #findProperties(Class, Class...)}.
     *
     * @param entityType
     * @param annotations
     * @return
     */
    @SafeVarargs
    public static Stream<Field> streamProperties(final Class<?> entityType, final Class<? extends Annotation>... annotations) {
        return getFieldsAnnotatedWith(entityType, true, IsProperty.class, annotations);
    }

    public static Stream<Field> streamDeclaredProperties(final Class<?> entityType) {
        return streamDeclaredFields(entityType)
                .filter(field -> AnnotationReflector.isAnnotationPresent(field, IsProperty.class));
    }

    /**
     * Returns "real" properties (fields annotated with {@link IsProperty}) for <code>entityType</code> that are also annotated with specified <code>annotations</code> (if any).
     * Refer issue <a href='https://github.com/fieldenms/tg/issues/1729'>#1729</a> for more details.
     * <p>
     * For union entities (i.e. those extending {@link AbstractUnionEntity}), properties {@code key} and {@code desc} are not considered to be "real" properties.
     * <p>
     * For product entities, property {@code desc} is excluded if {@link EntityUtils#hasDescProperty(Class)} returns {@code false}.<br>
     * And property {@code key} is excluded if {@link EntityUtils#isCompositeEntity(Class)} returns {@code true}.
     *
     * @param entityType
     * @param annotations
     *
     * @return
     */
    @SafeVarargs
    public static List<Field> findRealProperties(final Class<? extends AbstractEntity<?>> entityType, final Class<? extends Annotation>... annotations) {
        return streamRealProperties(entityType, annotations).collect(toList());
    }

    /**
     * A stream equivalent to method {@link #findRealProperties(Class, Class...)}.
     *
     * @param entityType
     * @param annotations
     * @return
     */
    @SafeVarargs
    public static Stream<Field> streamRealProperties(final Class<? extends AbstractEntity<?>> entityType, final Class<? extends Annotation>... annotations) {
        final boolean hasDesc = hasDescProperty(entityType);
        final boolean hasCompositeKey = isCompositeEntity(entityType);
        final boolean isUnion = isUnionEntityType(entityType);
        return getFieldsAnnotatedWith(entityType, false, IsProperty.class, annotations)
               .filter(f -> (hasDesc          && !isUnion || !DESC.equals(f.getName()))  // if not hasDesc     or isUnion then exclude DESC
                         && (!hasCompositeKey && !isUnion || !KEY.equals(f.getName()))); // if hasCompositeKey or isUnion then exclude KEY
    }

    /**
     * Returns the list of properties that could be used in lifecycle reporting.
     *
     * @return
     */
    public static List<Field> findLifecycleProperties(final Class<? extends AbstractEntity<?>> type) {
        final List<Field> properties = findProperties(type, Monitoring.class);
        properties.remove(getFieldByName(type, KEY));
        properties.remove(getFieldByName(type, DESC));
        final List<Field> keys = getKeyMembers(type);
        properties.removeAll(keys);
        return properties;
    }

    /**
     * Returns list of properties of the entity class that are entities themselves.
     *
     * @param entityType
     * @return
     */
    public static List<Field> findPropertiesThatAreEntities(final Class<?> entityType) {
        final List<Field> properties = findProperties(entityType);

        for (final Iterator<Field> iter = properties.iterator(); iter.hasNext();) {
            final Field property = iter.next();
            if (!AbstractEntity.class.isAssignableFrom(property.getType())) {
                iter.remove();
            }
        }
        return properties;
    }

    /**
     * Returns a list of properties of the specified type that are declared in the provided entity type and are annotated with specified <code>annotations</code> (if any).
     *
     * @param entityType
     * @param propertyType
     * @param annotations
     * @return
     */
    public static List<Field> findPropertiesOfSpecifiedType(final Class<?> entityType, final Class<?> propertyType, final Class<? extends Annotation>... annotations) {
        final List<Field> properties = findProperties(entityType, annotations);

        for (final Iterator<Field> iter = properties.iterator(); iter.hasNext();) {
            final Field property = iter.next();
            if (!propertyType.isAssignableFrom(property.getType())) {
                iter.remove();
            }
        }
        return properties;
    }

    /**
     * Determines properties within the provided class to be used for a key. There are two cases: either entity uses a composite key or a single property <code>key</code> represent
     * a key.
     * <p>
     * The implementation of this method traverses the whole class hierarchy. Thus, it supports correct
     * determination of properties declared at different hierarchical levels constituting a part of the composite key.
     *
     * IMPORTANT: all key members for types are cached during application lifecycle. It greatly reduces computational complexity as there is no need to retrieve key members for
     * immutable {@link AbstractEntity}'s descendants.
     *
     * @param type
     * @return
     */
    public static final List<Field> getKeyMembers(final Class<? extends AbstractEntity<?>> type) {
        // NOTE: please note that perhaps for generated types the key members are often similar as in
        // original types. But this is not the case when someone changed a key member property
        // by adding another property inside it etc. (this is fully allowed in TG). Thus such an optimisation
        // of this cache needs to be investigated more carefully.
        //
        // final Class<?> referenceTypeFromWhichKeyMembersCanBeDetermined = DynamicEntityClassLoader.getOriginalType(type); ?

        try {
            return new ArrayList<>(ENTITY_KEY_MEMBERS.get(type, () -> Collections.unmodifiableList(loadKeyMembers(type))));
        } catch (final Exception ex) {
            throw new ReflectionException("Could not get key members for type [%s]".formatted(type), ex);
        }
    }

    /**
     * Determines properties within the provided class to be used for a key. There are two cases: either entity uses a composite key or a single property <code>key</code> represent
     * a key.
     * <p>
     * The implementation of this method traverses the whole class hierarchy. Thus, it supports correct
     * determination of properties declared at different hierarchical levels constituting a part of the composite key.
     *
     * @param type
     * @return
     */
    private static final List<Field> loadKeyMembers(final Class<? extends AbstractEntity<?>> type) {
        // the use of SortedMap ensures the correct order of properties to be used for composite key
        final SortedMap<Integer, Field> properties = new TreeMap<>();
        final List<Field> compositeKeyFields = findRealProperties(type, CompositeKeyMember.class);

        for (final Field field : compositeKeyFields) {
            final CompositeKeyMember annotation = AnnotationReflector.getAnnotation(field, CompositeKeyMember.class);
            final int order = annotation.value();
            if (properties.containsKey(order)) {
                throw new ReflectionException(
                        format("Annotation [%s] in class [%s] for property [%s] has a duplicate order value of [%s], which is already present in property [%s].",
                        CompositeKeyMember.class.getName(), type.getName(), field.getName(), order, properties.get(order)));
            }
            properties.put(order, field);
        }
        final List<Field> keyMembers = new ArrayList<>(properties.values());
        // if there are no fields annotated with CompositeKeyMember then this
        // entity uses a non-composite (simple) key.
        if (keyMembers.isEmpty()) {
            keyMembers.add(getFieldByName(type, KEY));
        }
        return keyMembers;
    }

    // ======================================================================================================
    ///////////////////////////////////// Finding/getting fields and their values ///////////////////////////

    /// Finds a field (including private, protected and public) by name in the type's hierarchy.
    ///
    /// Throws an exception if the field was not found.
    ///
    @SuppressWarnings("unchecked")
    public static Field getFieldByName(final Class<?> type, final String name) {
        if (AbstractUnionEntity.class.isAssignableFrom(type)) {
            final var unionType = (Class<? extends AbstractUnionEntity>) type;
            if (commonPropertiesForUnion(unionType).contains(name)) {
                return getFieldByName(unionProperties(unionType).getFirst().getType(), name);
            }
        }

        Class<?> klass = type;
        while (klass != Object.class) {
            for (final var field : klass.getDeclaredFields()) {
                if (name.equals(field.getName())) {
                    return field;
                }
            }
            klass = klass.getSuperclass();
        }

        throw new ReflectionException(format("Failed to locate field [%s] in type [%s]", name, type.getName()));
    }

    /// The same as [#getFieldByName(Class,String)], but side effect free.
    ///
    public static Optional<Field> getFieldByNameOptionally(final Class<?> type, final String name) {
        final Either<Exception, Field> result = Try(() -> getFieldByName(type, name));
        if (result instanceof Right<?, Field>(var field)) {
            return Optional.of(field);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Finds field (including private, protected and public) by name in the type's hierarchy.
     * <p>
     * This method supports a dot notation (e.g. property.properties-property) -- this is its main difference with {@link #getFieldByName(Class, String)}.
     * <p>
     * Throws {@link IllegalArgumentException} if field/method was not found by its dot-expression.
     * <p>
     * Throws {@link MethodFoundException} if method was found by its dot-expression (but no field could be retrieved in this case).
     * <p>
     *
     * @param type
     * @param dotExpr
     *            -- dot-expression field/method definition (e.g. "prop1.prop2", "prop1.method2()", "method1().prop2", "method1().method2()")
     * @return
     */
    public static Field findFieldByName(final Class<?> type, final CharSequence dotExpr) {
        return findFieldByNameWithOwningType(type, dotExpr)._2;
    }

    /**
     * The same as {@link #findFieldByName(Class, CharSequence)}, but the returned tuple includes the type, where the last property or method in the {@code dotExpr} belongs.
     * This could a declaring type, but also the last type reached during the path traversal.
     *
     * @param type
     * @param dotExprr
     * @return
     */
    public static T2<Class<?>, Field> findFieldByNameWithOwningType(final Class<?> type, final CharSequence dotExprr) {
        // check if passed "dotExprr" is correct:
        PropertyTypeDeterminator.determinePropertyType(type, dotExprr);
        if (dotExprr.toString().endsWith("()")) {
            throw new MethodFoundException("Illegal situation: a method was found from the dot-expression expression == [" + dotExprr + "]");
        }
        final Pair<Class<?>, String> transformed = PropertyTypeDeterminator.transform(type, dotExprr);
        return t2(transformed.getKey(), getFieldByName(transformed.getKey(), transformed.getValue()));
    }

    /**
     * The same as {@link Finder#findFieldByName(Class, CharSequence)}, but side effect free.
     *
     * @param type
     * @param dotExpr
     * @return
     */
    public static Optional<Field> findFieldByNameOptionally(final Class<?> type, final String dotExpr) {
        final Either<Exception, Field> result = Try(() -> findFieldByName(type, dotExpr));
        if (result instanceof Right) {
            return Optional.of(((Right<Exception, Field>) result).value());
        } else {
            return Optional.empty();
        }
    }

    /**
     * This method is similar to {@link #findFieldByName(Class, CharSequence)}, but returns property values rather than type information.
     */
    public static <T> T findFieldValueByName(final AbstractEntity<?> entity, final String dotExpr) {
        if (entity == null) {
            return null;
        }
        final String[] propNames = splitPropPathToArray(dotExpr);
        Object value = entity;
        for (final String propName : propNames) {
            value = getPropertyValue((AbstractEntity<?>) value, propName);

            if (value == null) {
                return null;
            }
        }
        return (T) value;
    }

    /**
     * Searches through the owner type hierarchy for all fields of the type assignable to the provided field type.
     * This method excludes static fields.
     *
     * @param ownerType
     * @param fieldType
     * @return list of found fields, which can be empty
     */
    public static SequencedSet<Field> getFieldsOfSpecifiedType(final Class<?> ownerType, final Class<?> fieldType) {
        return getFieldsOfSpecifiedTypes(ownerType, CollectionUtil.linkedSetOf(fieldType));
    }

    /**
     * Searches through the owner type hierarchy for all fields of the type assignable to one of the provided field types.
     * This method excludes static fields.
     *
     * @param ownerType
     * @param fieldTypes
     * @return list of found fields, which can be empty
     */
    public static SequencedSet<Field> getFieldsOfSpecifiedTypes(final Class<?> ownerType, final SequencedSet<Class<?>> fieldTypes) {
        if (fieldTypes == null || fieldTypes.isEmpty()) {
            throw new IllegalArgumentException("The list of types should be non-empty.");
        }
        final SequencedSet<Field> fields = new LinkedHashSet<>();
        // Need to iterated thought the type hierarchy to process all declared fields.
        for (Class<?> type = ownerType; type != Object.class; type = type.getSuperclass()) {
            // Iterate though the list of fields declared in the class represented by type variable, and add those of the specified field type
            Arrays.stream(type.getDeclaredFields()).filter(field -> !Modifier.isStatic(field.getModifiers()) && isAssignableFrom(field.getType(), fieldTypes)).forEach(fields::add);
        }
        return fields;
    }

    public static boolean isAssignableFrom(final Class<?> askedType, final SequencedSet<Class<?>> fieldTypes) {
        if (fieldTypes == null || fieldTypes.isEmpty()) {
            throw new IllegalArgumentException("The list of types should be non-empty.");
        }
        for (final Class<?> fType : fieldTypes) {
            if (fType.isAssignableFrom(askedType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns fields of the specified class that extends {@link AbstractUnionEntity}.
     *
     * @param type
     * @return
     */
    private static List<Field> getUnionEntityFields(final Class<? extends AbstractUnionEntity> type) {
        final List<Field> fields = new ArrayList<>();
        final List<Field> unionProperties = AbstractUnionEntity.unionProperties(type);
        final Set<String> commonProperties = AbstractUnionEntity.commonProperties(type);
        for (final String propertyName : commonProperties) {
            fields.add(getFieldByName(unionProperties.get(0).getType(), propertyName));
        }
        fields.addAll(unionProperties);
        return fields;
    }

    /**
     * Traces through specified list of fields and returns a stream of those annotated with allAnnotations.
     *
     * @param fields
     * @param allAnnotations
     * @return
     */
    private static Stream<Field> streamFieldsAnnotatedWith(final List<Field> fields, final Collection<Class<? extends Annotation>> allAnnotations) {
        return fields.stream().filter(field -> {
            int count = 0;
            for (final Class<? extends Annotation> annotation : allAnnotations) {
                if (AnnotationReflector.isAnnotationPresent(field, annotation)) {
                    count++;
                }
            }
            return count == allAnnotations.size();
        });
    }

    /**
     * Returns a list of fields (including private, protected and public). This method processes the whole class hierarchy.
     *
     * @param type
     * @param withUnion
     *            - determines whether include union entitie's properties (i.e. common properties, union properties) or just simple union entity fields.
     * @return
     */
    public static List<Field> getFields(final Class<?> type, final boolean withUnion) {
        final List<Field> properties = new ArrayList<>();
        Class<?> klass = type;
        if (withUnion && AbstractUnionEntity.class.isAssignableFrom(klass)) {
            properties.addAll(getUnionEntityFields((Class<AbstractUnionEntity>) type));
        } else {
            while (klass != Object.class) { // need to iterated thought
                // hierarchy in order to retrieve
                // fields from above the current
                // instance
                // iterate though the list of fields declared in the class
                // represented by klass variable, and add those annotated with
                // the specified annotation
                properties.addAll(Arrays.asList(klass.getDeclaredFields()));
                // move to the upper class in the hierarchy in search for more
                // fields
                klass = klass.getSuperclass();
            }
        }

        return removeDuplicates(properties);
    }

    private static List<Field> removeDuplicates(final List<Field> wholeHierarchyProperties) {
        final List<Field> properties = new ArrayList<>();
        final List<Field> keyProps = new ArrayList<>();
        final Set<String> fieldNames = new HashSet<>();
        for (final Field field : wholeHierarchyProperties) {
            if (!fieldNames.contains(field.getName())) {
                fieldNames.add(field.getName());
                if (isKeyOrKeyMember(field)) {
                    keyProps.add(field);
                } else {
                    properties.add(field);
                }
            }
        }

        // prepend key properties at the beginning of the list of properties
        // this is essential for serialisation to correctly initialise collectional properties
        final List<Field> propertiesWithKeys = new ArrayList<>(keyProps);
        propertiesWithKeys.addAll(properties);

        return propertiesWithKeys;
    }

    public static boolean isKeyOrKeyMember(final Field field) {
        return AbstractEntity.KEY.equals(field.getName()) || field.isAnnotationPresent(CompositeKeyMember.class);
    }

    /**
     * Returns a stream of fields (including private, protected and public) annotated with the specified annotation. This method processes the whole class hierarchy.
     *
     * @param type
     * @param withUnion - determines whether include union entitie's properties (i.e. common properties, union properties) or just simple union entity fields.
     * @param annot
     * @param annotations
     *
     * @return
     */
    private static Stream<Field> getFieldsAnnotatedWith(final Class<?> type, final boolean withUnion, final Class<? extends Annotation> annot, final Class<? extends Annotation>... annotations) {
        final Set<Class<? extends Annotation>> allAnnotations = new HashSet<>();
        allAnnotations.add(annot);
        allAnnotations.addAll(Arrays.asList(annotations));
        return streamFieldsAnnotatedWith(getFields(type, withUnion), allAnnotations);
    }

    private static Stream<Field> streamDeclaredFields(final Class<?> type) {
        return Arrays.stream(type.getDeclaredFields());
    }

    /**
     * Returns value of the {@link AbstractUnionEntity} field specified with property.
     *
     * @param value
     * @param property
     * @return
     * @throws IllegalAccessException
     */
    private static final Set<String> KEY_DESC_ID = setOf(KEY, DESC, ID);
    private static Object getAbstractUnionEntityFieldValue(final AbstractUnionEntity value, final String property) {
        final Optional<Field> field;
        final Object valueToRetrieveFrom;
        final List<String> unionProperties = getFieldNames(unionProperties(value.getClass()));
        final Set<String> commonProperties = commonProperties(value.getClass());

        try {
            if (unionProperties.contains(property)) { // union properties:
                field = ofNullable(getFieldByName(value.getClass(), property));
                valueToRetrieveFrom = value;
            } else if (commonProperties.contains(property) || KEY_DESC_ID.contains(property)) { // common property (perhaps with KEY / DESC) or non-common KEY, DESC and ID for which the value can still be taken
                final AbstractEntity<?> activeEntity = value.activeEntity();
                field = ofNullable(activeEntity != null ? getFieldByName(activeEntity.getClass(), property) : null);
                valueToRetrieveFrom = activeEntity;
            } else { // not-properly specified property:
                throw new ReflectionException(format("Property [%s] is not properly specified. Maybe \"activeEntity.\" prefix should be explicitly specified.", property));
            }
            return field.isPresent() ? getFieldValue(field.get(), valueToRetrieveFrom) : null;
        } catch (final Exception e) {
            throw new ReflectionException(format("Could not obtain value of property [%s] for union entity [%s]. Potentially \"activeEntity.\" prefix should be explicitly specified.", property, value.getType().getName()), e);
        }
    }

    /**
     * Returns method value for {@link AbstractUnionEntity} instance.
     *
     * @param instance
     * @param methodName
     * @param arguments
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private static Object getAbstractUnionEntityMethodValue(final AbstractUnionEntity instance, final String methodName, final Class<?>... arguments) throws NoSuchMethodException {
        try {
            final Method method = Reflector.getMethodForClass(instance.getClass(), methodName, arguments);
            return getMethodValue(method, instance);
        } catch (final NoSuchMethodException e) {
            final AbstractEntity<?> activeEntity = instance.activeEntity();
            if (activeEntity != null && AbstractUnionEntity.commonMethodNames((Class<AbstractUnionEntity>) instance.getType()).contains(methodName)) {
                final Method method = Reflector.getMethodForClass(activeEntity.getClass(), methodName, arguments);
                return getMethodValue(method, activeEntity);
            } else {
                throw new ReflectionException(format("Active entity can not be null for union entity of type [%s]", instance.getType().getSimpleName()));
            }
        }
    }

    /**
     * Returns field value for the {@code valueToRetrievefrom} object.
     *
     * @param field
     * @param valueToRetrieveFrom
     * @return
     */
    public static Object getFieldValue(final Field field, final Object valueToRetrieveFrom) {
        final boolean isAccessible = field.isAccessible();
        field.setAccessible(true);
        final Object value;
        try {
            value = field.get(valueToRetrieveFrom);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new ReflectionException(format("Could not access field [%s] in type [%s].", field.getName(), valueToRetrieveFrom.getClass().getSimpleName()), e);
        }
        field.setAccessible(isAccessible);
        return value;
    }

    /**
     * Invokes specified method on given {@code objectToInvoceOn}.
     *
     * @param method
     * @param objectToInvokeOn
     * @return
     */
    private static Object getMethodValue(final Method method, final Object objectToInvokeOn) {
        final boolean isAccessible = method.isAccessible();
        method.setAccessible(true);
        final Object value;
        try {
            value = method.invoke(objectToInvokeOn);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new ReflectionException(format("Could not access method [%s] in type [%s].", method.getName(), objectToInvokeOn.getClass().getSimpleName()), e);
        }
        method.setAccessible(isAccessible);
        return value;
    }

    /**
     * Returns a value of property identified by {@code propOrGetterName} for {@code entity}.
     * As parameter {@code propOrGetterName} suggests, this method accepts either a property field name or the name of its getter method.
     *
     * @param entity
     * @param propOrGetterName
     * @return
     * @throws IllegalAccessException
     */
    public static Object getPropertyValue(final AbstractEntity<?> entity, final String propOrGetterName) {
        final Object value;
        if (!propOrGetterName.contains("()")) {
            if (entity instanceof AbstractUnionEntity) {
                value = getAbstractUnionEntityFieldValue((AbstractUnionEntity) entity, propOrGetterName);
            } else {
                value = getFieldValue(getFieldByName(entity.getClass(), propOrGetterName), entity);
            }
        } else {
            try {
                if (entity instanceof AbstractUnionEntity) {
                    value = getAbstractUnionEntityMethodValue((AbstractUnionEntity) entity, propOrGetterName.substring(0, propOrGetterName.length() - 2));
                } else {
                    value = getMethodValue(Reflector.getMethod(entity.getClass(), propOrGetterName.substring(0, propOrGetterName.length() - 2)), entity);
                }
            } catch (final NoSuchMethodException e) {
                throw new IllegalArgumentException("Failed to locate parameterless method " + propOrGetterName + " in " + entity.getClass(), e);
            }
        }
        return value;
    }

    // ========================================================================================================
    /////////////////////////////// Miscellaneous utilities ///////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    public static Stream<Class<? extends AbstractEntity<?>>> streamUnionMembers(final Class<? extends AbstractUnionEntity> unionEntityType) {
        return unionProperties(unionEntityType).stream()
                .map(field -> (Class<? extends AbstractEntity<?>>) field.getType());
    }

    /// Returns a set of properties that are present in all of the specified types.
    ///
    static SequencedSet<String> findCommonProperties(final List<Class<? extends AbstractEntity<?>>> entityTypes) {
        // (EntityType, Properties)
        final var pairs = entityTypes.stream().map(t -> t2(t, findRealProperties(t))).toList();

        return first(pairs).map(fstPair -> {
                    final var restPairs = pairs.subList(1, pairs.size());
                    return fstPair.map((fstType, fstProps) -> fstProps.stream()
                                                                      .filter(fstProp -> restPairs.stream()
                                                                                                  .allMatch(pair -> pair.map((otherType, otherProps) -> isPropertyPresent(fstProp, fstType, otherProps, otherType)))));
                })
                .orElseGet(Stream::of)
                .map(Field::getName)
                .collect(collectingAndThen(toCollection(LinkedHashSet::new), Collections::unmodifiableSequencedSet));
    }

    /// Returns a set of property names that are common to all members of the specified union type.
    ///
    public static SequencedSet<String> commonPropertiesForUnion(final Class<? extends AbstractUnionEntity> unionType) {
        try {
            return UNION_COMMON_PROPERTIES.get(PropertyTypeDeterminator.baseEntityType(unionType), () -> commonPropertiesForUnion_(unionType));
        } catch (final ExecutionException e) {
            throw new ReflectionException(e.getCause());
        }
    }

    private static SequencedSet<String> commonPropertiesForUnion_(final Class<? extends AbstractUnionEntity> unionType) {
        return findCommonProperties(streamUnionMembers(unionType).toList());
    }

    /// Returns a list of properties that represent the members of the specified union type.
    ///
    public static List<Field> unionProperties(final Class<? extends AbstractUnionEntity> unionType) {
        try {
            return UNION_MEMBER_PROPERTIES.get(PropertyTypeDeterminator.baseEntityType(unionType), () -> unionProperties_(unionType));
        } catch (final ExecutionException e) {
            throw new ReflectionException(e.getCause());
        }
    }

    private static List<Field> unionProperties_(final Class<? extends AbstractUnionEntity> unionType) {
        return streamRealProperties(unionType)
                .filter(prop -> AbstractEntity.class.isAssignableFrom(prop.getType()))
                .collect(toImmutableList());
    }

    /// Returns a stream that contains the full paths to `subProp` for each union member that has this property.
    ///
    /// @param subProp a simple property name
    ///
    public static Stream<String> streamUnionSubProperties(final Class<? extends AbstractUnionEntity> unionType, final CharSequence subProp) {
        return streamUnionMembersWithSubProperty(unionType, subProp)
                .map(memberName -> memberName + "." + subProp);
    }

    /// Returns a stream that contains the names of those union members that have property `subProp`.
    ///
    /// By definition, if `subProp` is a common property, the resulting stream contains all union members.
    ///
    /// @param subProp a simple property name
    ///
    public static Stream<String> streamUnionMembersWithSubProperty(final Class<? extends AbstractUnionEntity> unionType, final CharSequence subProp) {
        if (subProp.toString().contains(".")) {
            throw new InvalidArgumentException("[subProp] must be a simple property name. Invalid value: %s".formatted(subProp));
        }

        // `isPropertyPresent` will not at all identify `id`, and will not take `@DescTitle` into account for `desc`.
        if (ID.contentEquals(subProp)) {
            return unionProperties(unionType).stream().map(Field::getName);
        }
        else if (DESC.contentEquals(subProp)) {
            return unionProperties(unionType)
                    .stream()
                    .filter(memberField -> hasDescProperty((Class<? extends AbstractEntity<?>>) memberField.getType()))
                    .map(Field::getName);
        }
        else {
            return unionProperties(unionType)
                    .stream()
                    .filter(memberField -> isPropertyPresent(memberField.getType(), subProp.toString()))
                    .map(Field::getName);
        }
    }


    /// Returns `true` if `prop` is present among `otherProps`.
    /// In the case of property `key`, special care is taken to determine its type.
    ///
    /// @param propOwner  the type where `prop` is present, required to correctly identify the type of property `key`.
    /// @param otherPropsOwner  the type where `otherProps` are present, required to correctly identify the type of property `key`.
    ///
    private static boolean isPropertyPresent(final Field prop, final Class<? extends AbstractEntity<?>> propOwner, final List<Field> otherProps, final Class<? extends AbstractEntity<?>> otherPropsOwner) {
        return otherProps.stream()
                .filter(otherProp -> prop.getName().equals(otherProp.getName()))
                .anyMatch(otherProp -> {
                    final boolean isKey = KEY.equals(prop.getName());
                    final var propType = isKey ? getKeyType(propOwner) : prop.getType();
                    final var otherPropType = isKey ? getKeyType(otherPropsOwner) : otherProp.getType();
                    return propType != null && otherPropType != null && propType.equals(otherPropType);
                });
    }

    /**
     * Returns <code>true</code> if property (defined by <code>dotExpr</code>) is present in type <code>forType</code>. Field should have {@link IsProperty} annotation
     * assigned to be recognised as "property".
     *
     * @param forType
     * @param dotExpr
     * @return
     */
    public static boolean isPropertyPresent(final Class<?> forType, final String dotExpr) {
        try {
            return AnnotationReflector.getPropertyAnnotation(IsProperty.class, forType, dotExpr) != null;
        } catch (final MethodFoundException mfe) {
            return false;
        } catch (final ReflectionException iae) {
            return false;
        }
    }

    /**
     * Extracts field names.
     *
     * @param fields
     * @return
     */
    public static List<String> getFieldNames(final List<Field> fields) {
        final List<String> result = new ArrayList<>();
        for (int index = 0; index < fields.size(); index++) {
            result.add(fields.get(index).getName());
        }
        return result;
    }

    /**
     * A contract for ignoring properties and entity types during composition of property paths.
     *
     */
    public interface IPropertyPathFilteringCondition {
        boolean ignore(String propertyName);

        boolean ignore(Class<?> entityType);
    }

    /**
     * Marks exceptional situations where a method was found in a dot-expression expression.
     *
     * @author TG Team
     *
     */
    public static class MethodFoundException extends ReflectionException {
        private static final long serialVersionUID = 1L;

        public MethodFoundException(final String message) {
            super(message);
        }
    }

    /**
     * Method to recursively find properties of the specified type in the provided entity type and build dot-notated paths.
     *
     * TODO Need to provide support for handling union entities, which requires a special treatment -- should return a pair of enclosing union entity property and the enclosed
     * property. For example, in case of WorkOrder while searching for property of type Workshop a pair of <code>workorderable.vehicle</code> and
     * <code>workorderable.vehicle.station.sector.workshop</code> should be returned.
     *
     * @param entityType
     * @param propertyType
     * @return
     */
    public static List<String> findPathsForPropertiesOfType(final Class<? extends AbstractEntity> entityType, final Class<? extends AbstractEntity> propertyType, final IPropertyPathFilteringCondition filter) {
        if (entityType == propertyType) {
            return new ArrayList<String>() {
                {
                    add("id");
                }
            };
        }
        return findPathsForPropertiesOfType("", new ArrayList<Class<?>>() {
            {
                add(entityType);
            }
        }, propertyType, filter);
    }

    /** This is an actual implementation of the above method. */
    private static List<String> findPathsForPropertiesOfType(final String name, final List<Class<?>> entityTypes, final Class<? extends AbstractEntity> propertyType, final IPropertyPathFilteringCondition filter) {
        final List<String> result = new ArrayList<>();

        if (filter.ignore(entityTypes.get(entityTypes.size() - 1))) {
            return result;
        }

        final List<Field> fields = findPropertiesThatAreEntities(entityTypes.get(entityTypes.size() - 1));
        for (final Field field : fields) {
            if (!filter.ignore(field.getName())) {
                if (propertyType.equals(field.getType())) {
                    result.add((!StringUtils.isEmpty(name) ? name + "." : "") + field.getName());
                } else if (!entityTypes.contains(field.getType())) {
                    entityTypes.add(field.getType());
                    result.addAll(findPathsForPropertiesOfType((!StringUtils.isEmpty(name) ? name + "." : "") + field.getName(), entityTypes, propertyType, filter));
                    entityTypes.remove(field.getType());
                }
            }
        }
        return result;
    }

    /**
     * Looks for the value of <code>linkProperty</code> for the specified property in a form of dot notation expression starting with a given type. The value of
     * <code>linkProperty</code> is either read from property annotation or dynamically determined based on its type and key composition.
     * <p>
     * In case <code>linkProperty</code> could not be either read or determined, a runtime exception is thrown.
     * <p>
     * This method covers situation of all possible entity associations, including One-to-One where <code>linkProperty</code> is not present.
     *
     * @param type
     * @param dotExpr
     * @return
     */
    public static String findLinkProperty(final Class<? extends AbstractEntity<?>> type, final String dotExpr) {
        final Field field = Finder.findFieldByName(type, dotExpr);
        if (!AnnotationReflector.isAnnotationPresent(field, IsProperty.class)) {
            throw new ReflectionException("Field " + dotExpr + " in type " + type.getName() + " is not a property.");
        }

        final IsProperty propAnnotation = AnnotationReflector.getAnnotation(field, IsProperty.class);
        // check if meta-data is present and if so use it
        if (!DEFAULT_LINK_PROPERTY.equals(propAnnotation.linkProperty())) {
            return propAnnotation.linkProperty();
        }
        // otherwise try to determine link property dynamically based on property type and composite key
        final Class<?> masterType = DynamicEntityClassLoader.getOriginalType(field.getDeclaringClass());
        final Class<? extends AbstractEntity<?>> propType = (Class<? extends AbstractEntity<?>>) PropertyTypeDeterminator.determinePropertyType(type, dotExpr);
        final boolean collectionalProp = PropertyTypeDeterminator.isCollectional(type, dotExpr);

        // to be in association property should be an entity type
        if (!AbstractEntity.class.isAssignableFrom(propType)) {
            throw new IllegalArgumentException("Property " + dotExpr + " in type " + type.getName() + " is not an entity (" + propType.getName() + ").");
        }

        // non-collectional properties must have their linkProperty specified explicitly, otherwise they're considered to be Many-to-One
        if (!collectionalProp && !isOne2One_association(type, dotExpr)) {
            throw new IllegalStateException("Non-collectional property " + dotExpr + " in type " + type.getName() + //
            " represents a Many-to-One association.");
        }

        // first check for a link property amongst key members
        final Pair<Integer, String> keyPair = lookForLinkProperty(masterType, getKeyMembers(propType));
        final Integer matchingKeyMemembersCount = keyPair.getKey(); // the number of matched fields that are key members
        final String keylinkProperty = keyPair.getValue(); // the matched key member name
        // check if a key member was found
        if (matchingKeyMemembersCount == 0) {
            throw new IllegalArgumentException("Property " + dotExpr + " in type " + type.getName() + " does not have an appropriate key member of type "
                    + masterType.getName() + ".");
        } else if (matchingKeyMemembersCount > 1) {
            // more than one matching key member means ambiguity
            throw new IllegalArgumentException("Property " + dotExpr + " in type " + type.getName() + " has more than one key of type " + masterType.getName() + ".");
        }
        // otherwise, a single possible link property has been found
        return keylinkProperty;
    }

    /** A helper function to assist in checking field for a potential linkProperty role match. */
    private static Pair<Integer, String> lookForLinkProperty(final Class<?> masterType, final List<Field> fieldsToCheck) {
        String linkProperty = null;
        int count = 0;
        for (final Field field : fieldsToCheck) {
            final Class<?> fieldType = DynamicEntityClassLoader.getOriginalType(field.getType());
            if (fieldType.isAssignableFrom(masterType)) {
                linkProperty = field.getName();
                count++;
            }
        }

        return new Pair<>(count, linkProperty);
    }

    /**
     * Determines whether a specified property represents either one-2-many or one-2-one association.
     *
     * @param type
     * @param dotExpr
     * @return
     */
    public static boolean isOne2Many_or_One2One_association(final Class<? extends AbstractEntity<?>> type, final String dotExpr) {
        return isOne2One_association(type, dotExpr) || hasLinkProperty(type, dotExpr);
    }

    public static boolean hasLinkProperty(final Class<? extends AbstractEntity<?>> type, final String dotExpr) {
        // if it is not one-to-one than may be it is one-to-many
        // for this we should try to identify linkProperty, it is identifiable then return true, otherwise -- false
        try {
            return !StringUtils.isEmpty(findLinkProperty(type, dotExpr));
        } catch (final Exception ex) {
            // exception is possible in various cases of incorrectly constructed associations, which should not be recognised as valid one-to-many
            return false;
        }
    }

    /**
     * Determines whether a specified property represents a one-2-one association.<br>
     * <p>
     * <h6>The rule</h6>
     * A property of an entity type represents a one-2-one association if any of the following conditions hold:
     * <ol>
     *   <li>The property key is of the same type as the entity where it is declared (main entity), or
     *   <li>The property key is assignable from the main entity and the main entity has a real property "id".
     * </ol>
     * <i>Note:</i> An entity has a real (not just declared) property "id" only if it is a persistent entity or a synthetic entity that is based on persistent.
     *
     * @param type
     * @param dotExpr
     * @return
     */
    public static boolean isOne2One_association(final Class<? extends AbstractEntity<?>> type, final String dotExpr) {
        final Class<?> propertyType = PropertyTypeDeterminator.determinePropertyType(type, dotExpr);
        if (isEntityType(propertyType)) {
            final Class<? extends AbstractEntity<?>> mainType = DynamicEntityClassLoader.getOriginalType(PropertyTypeDeterminator.transform(type, dotExpr).getKey());
            final Class<?> propertyTypeKeyType = DynamicEntityClassLoader.getOriginalType(PropertyTypeDeterminator.determinePropertyType(propertyType, KEY));

            return // either property type's key is of the same type as the main (declaring) entity type
                   propertyTypeKeyType == mainType ||
                   // or the property type's key is compatible with the main entity type, which covers two possible cases:
                   // 1. a persistent entity that extends another persistent entity;
                   // 2. a synthetic entity, derived from a persistent entity (synthetic with ID).
                   propertyTypeKeyType.isAssignableFrom(mainType) && (isPersistentEntityType(propertyTypeKeyType) || isSyntheticBasedOnPersistentEntityType((Class<? extends AbstractEntity<?>>) propertyTypeKeyType));
        }
        return false;
    }

}
