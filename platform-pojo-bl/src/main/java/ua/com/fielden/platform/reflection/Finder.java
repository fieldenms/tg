package ua.com.fielden.platform.reflection;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.AbstractEntity.COMMON_PROPS;
import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Monitoring;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

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
    private final static Map<Class<?>, List<Field>> entityKeyMembers = new HashMap<>();

    /**
     * Let's hide default constructor, which is not needed for a static class.
     */
    private Finder() {
    }

    // ======================================================================================================
    ///////////////////////////////////// Finding/getting MetaProperties and PropertyDescriptors ////////////
    /**
     * Produces a list of property descriptors for a given entity type, including properties inherited from super a super type.
     *
     * @param <T>
     * @param entityType
     * @return
     */
    public static <T extends AbstractEntity<?>> List<PropertyDescriptor<T>> getPropertyDescriptors(final Class<T> entityType) {
        final List<PropertyDescriptor<T>> result = new ArrayList<PropertyDescriptor<T>>();
        for (final Field field : findProperties(entityType)) {
            result.add(new PropertyDescriptor<T>(entityType, field.getName()));
        }
        return result;
    }

    /**
     * Same as above, but instantiation happens using entity factory.
     *
     * @param <T>
     * @param entityType
     * @param factory
     * @return
     */
    public static <T extends AbstractEntity<?>> List<PropertyDescriptor<T>> getPropertyDescriptors(final Class<T> entityType, final EntityFactory factory) {
        final List<PropertyDescriptor<T>> result = new ArrayList<PropertyDescriptor<T>>();
        for (final Field field : findProperties(entityType)) {
            try {
                final PropertyDescriptor<T> pd = PropertyDescriptor.fromString(entityType.getName() + ":" + field.getName(), factory);
                result.add(pd);
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return result;
    }

    /**
     * Does much the same as {@link #findMetaProperty(AbstractEntity, String)}, but it retrieves all {@link MetaProperty}s specified in <code>dotNotationExp</code>. If, during
     * retrieval, some of the properties is null, then this method returns only that properties, which were retrieved.<br>
     * <br>
     *
     * @param entity
     * @param dotNotationExp
     * @return
     * @throws RuntimeException
     */
    public static List<MetaProperty> findMetaProperties(final AbstractEntity<?> entity, final String dotNotationExp) {
        final String[] properties = dotNotationExp.split(Reflector.DOT_SPLITTER);
        final List<MetaProperty> metaProperties = new ArrayList<MetaProperty>();
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
                throw new IllegalArgumentException("Failed to locate meta-property " + dotNotationExp + " starting with entity " + entity.getType() + ": " + entity);
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
     *
     * @param entity
     * @param dotNotationExp
     * @return
     * @throws RuntimeException
     */
    public static MetaProperty findMetaProperty(final AbstractEntity<?> entity, final String dotNotationExp) {
        final List<MetaProperty> metaProperties = findMetaProperties(entity, dotNotationExp);
        if (dotNotationExp.split(Reflector.DOT_SPLITTER).length > metaProperties.size()) {
            return null;
        } else {
            return metaProperties.get(metaProperties.size() - 1);
        }
    }

    /**
     * Obtains a set of meta-properties from an entity, sorted in a natural order as defined by {@link MetaProperty}.
     *
     * @param entity
     * @return
     * @throws RuntimeException
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
        return streamProperties(entityType, annotations).collect(Collectors.toList());
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

    /**
     * Returns "real" properties (fields annotated with {@link IsProperty}) for <code>entityType</code> that are also annotated with specified <code>annotations</code> (if any).
     * <p>
     * For {@link AbstractUnionEntity} <code>entityType</code>s common properties are disregarded.
     *
     * @param entityType
     * @param annotations
     *
     * @return
     */
    @SafeVarargs
    public static List<Field> findRealProperties(final Class<?> entityType, final Class<? extends Annotation>... annotations) {
        return streamRealProperties(entityType, annotations).collect(Collectors.toList());
    }

    /**
     * A stream equivalent to method {@link #findRealProperties(Class, Class...)}.
     * 
     * @param entityType
     * @param annotations
     * @return
     */
    @SafeVarargs
    public static Stream<Field> streamRealProperties(final Class<?> entityType, final Class<? extends Annotation>... annotations) {
        return getFieldsAnnotatedWith(entityType, false, IsProperty.class, annotations);
    }

    /**
     * Returns the list of properties that could be used in lifecycle reporting.
     *
     * @return
     */
    public static List<Field> findLifecycleProperties(final Class<? extends AbstractEntity> clazz) {
        final List<Field> properties = findProperties(clazz, Monitoring.class);
        properties.remove(getFieldByName(clazz, KEY));
        properties.remove(getFieldByName(clazz, DESC));
        final List<Field> keys = getKeyMembers(clazz);
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
     * Returns a list of properties of the specified type that are declared in the provided entity type.
     *
     * @param entityType
     * @param propertyType
     * @return
     */
    public static List<Field> findPropertiesOfSpecifiedType(final Class<?> entityType, final Class<?> propertyType) {
        final List<Field> properties = findProperties(entityType);

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
     * The implementation of this method is based on {@link #getFieldsAnnotatedWith(Class, Class)}, which traverses the whole class hierarchy. Thus, it supports correct
     * determination of properties declared at different hierarchical levels constituting a part of the composite key.
     *
     * IMPORTANT: all key members for types are cached during application lifecycle. It greatly reduces computational complexity as there is no need to retrieve key members for
     * immutable {@link AbstractEntity}'s descendants.
     *
     * @param klass
     * @return
     */
    public final static List<Field> getKeyMembers(final Class<?> type) {
        // NOTE: please note that perhaps for generated types the key members are often similar as in
        // original types. But this is not the case when someone changed a key member property
        // by adding another property inside it etc. (this is fully allowed in TG). Thus such an optimisation
        // of this cache needs to be investigated more carefully.
        //
        // final Class<?> referenceTypeFromWhichKeyMembersCanBeDetermined = DynamicEntityClassLoader.getOriginalType(type); ?

        final List<Field> cachedKeyMembers = entityKeyMembers.get(type); // TODO consider soft references here..
        return cachedKeyMembers == null ? new ArrayList<>(loadAndCacheKeyMembers(type)) : new ArrayList<>(cachedKeyMembers); // new list should be returned, not the same reference
        // old implementation -- return loadKeyMembers(type);
    }

    /**
     * Loads key members for <code>type</code> and caches them in global cache.
     *
     * @param type
     * @return
     */
    private final static List<Field> loadAndCacheKeyMembers(final Class<?> type) {
        final List<Field> loadedKeyMembers = Collections.unmodifiableList(loadKeyMembers(type)); // should be immutable
        entityKeyMembers.put(type, loadedKeyMembers);
        return loadedKeyMembers;
    }

    /**
     * Determines properties within the provided class to be used for a key. There are two cases: either entity uses a composite key or a single property <code>key</code> represent
     * a key.
     * <p>
     * The implementation of this method is based on {@link #getFieldsAnnotatedWith(Class, Class)}, which traverses the whole class hierarchy. Thus, it supports correct
     * determination of properties declared at different hierarchical levels constituting a part of the composite key.
     *
     * @param klass
     * @return
     */
    private final static List<Field> loadKeyMembers(final Class<?> type) {
        final SortedMap<Integer, Field> properties = new TreeMap<Integer, Field>(); // the use of SortedMap ensures the correct order of properties to be used the composite key
        final List<Field> compositeKeyFields = findRealProperties(type, CompositeKeyMember.class);

        for (final Field field : compositeKeyFields) {
            final CompositeKeyMember annotation = AnnotationReflector.getAnnotation(field, CompositeKeyMember.class);
            final int order = annotation.value();
            if (properties.containsKey(order)) {
                throw new IllegalArgumentException("Annotation " + CompositeKeyMember.class.getName() + " in class " + type.getName() + " for property '" + field.getName()
                        + "' has a duplicate order value of " + order + ", which is already present in property '" + properties.get(order) + "'.");
            }
            properties.put(order, field);
        }
        final List<Field> keyMembers = new ArrayList<Field>(properties.values());
        // if there where no fields annotated with CompositeKeyMember then this
        // entity uses a non-composite (simple) key.
        if (keyMembers.size() == 0) {
            keyMembers.add(getFieldByName(type, KEY));
        }
        return keyMembers;
    }

    // ======================================================================================================
    ///////////////////////////////////// Finding/getting fields and their values ///////////////////////////

    /**
     * Finds field (including private, protected and public) by name in the type's hierarchy.
     * <p>
     * Throws exception if field was not found.
     *
     * @param type
     * @param name
     * @return
     * @throws NoSuchFieldException
     */
    public static Field getFieldByName(final Class<?> type, final String name) {
        Class<?> klass = type;
        if (AbstractUnionEntity.class.isAssignableFrom(klass)) {
            final List<String> commonPropertiesList = AbstractUnionEntity.commonProperties((Class<AbstractUnionEntity>) type);
            if (commonPropertiesList.contains(name)) {
                return getFieldByName(AbstractUnionEntity.unionProperties(((Class<AbstractUnionEntity>) type)).get(0).getType(), name);
            }
        }
        while (klass != Object.class) { // need to iterated thought hierarchy in
            // order to retrieve fields from above
            // the current instance
            // iterate though the list of fields declared in the class
            // represented by klass variable
            for (final Field field : klass.getDeclaredFields()) {
                if (name.equals(field.getName())) {
                    return field;
                }
            }
            // move to the upper class in the hierarchy in search for more
            // fields
            klass = klass.getSuperclass();
        }
        throw new IllegalArgumentException("Failed to locate field " + name + " in " + type);
    }

    /**
     * Finds field (including private, protected and public) by name in the type's hierarchy.
     * <p>
     * This method supports a dot notation (e.g. property.properties-property) -- this is its main difference with {@link #getFieldByName(Class, String)}.
     * <p>
     * Throws {@link IllegalArgumentException} if field/method was not found by its dot-notation.
     * <p>
     * Throws {@link MethodFoundException} if method was found by its dot-notation (but no field could be retrieved in this case).
     * <p>
     *
     * @param type
     * @param dotNotationExp
     *            -- dot-notation field/method definition (e.g. "prop1.prop2", "prop1.method2()", "method1().prop2", "method1().method2()")
     * @return
     */
    public static Field findFieldByName(final Class<?> type, final String dotNotationExp) {
        // check if passed "dotNotationExp" is correct:
        PropertyTypeDeterminator.determinePropertyType(type, dotNotationExp);
        if (dotNotationExp.endsWith("()")) {
            throw new MethodFoundException("Legal situation : method was found by according dot-notation expression == [" + dotNotationExp + "]");
        }
        final Pair<Class<?>, String> transformed = PropertyTypeDeterminator.transform(type, dotNotationExp);
        return getFieldByName(transformed.getKey(), transformed.getValue());
    }

    /**
     * This method is similar to {@link #findFieldByName(Class, String)}, but returns property values rather than type information.
     *
     * @param instance
     * @param dotNotationExp
     * @return
     * @throws Exception
     */
    public static <T> T findFieldValueByName(final Object instance, final String dotNotationExp) throws Exception {
        if (instance == null) {
            return null;
        }
        final String[] properties = dotNotationExp.split(Reflector.DOT_SPLITTER);
        Object value = instance;
        for (final String property : properties) {
            value = getPropertyValue(value, property);
            if (value == null) {
                return null;
            }
        }
        return (T) value;
    }

    /**
     * Searches through the owner type hierarchy for all fields of the type assignable to the provided field type.
     *
     * @param ownerType
     * @param fieldType
     * @return list of found fields, which can be empty
     */
    public static List<Field> getFieldsOfSpecifiedType(final Class<?> ownerType, final Class<?> fieldType) {
        return getFieldsOfSpecifiedTypes(ownerType, Arrays.<Class<?>> asList(fieldType));
    }

    /**
     * Searches through the owner type hierarchy for all fields of the type assignable to the provided field type.
     *
     * @param ownerType
     * @param fieldType
     * @return list of found fields, which can be empty
     */
    public static List<Field> getFieldsOfSpecifiedTypes(final Class<?> ownerType, final List<Class<?>> fieldTypes) {
        if (fieldTypes == null || fieldTypes.isEmpty()) {
            throw new IllegalArgumentException("The list of types should be non-empty.");
        }
        final List<Field> fields = new ArrayList<Field>();
        for (Class<?> klass = ownerType; klass != Object.class; klass = klass.getSuperclass()) {
            // need to iterated thought hierarchy in order to retrieve fields from above the current instance
            // iterate though the list of fields declared in the class represented by klass variable, and add those of the specified field type
            final Field[] currFields = klass.getDeclaredFields();
            for (final Field field : currFields) {
                if (isAssignableFrom(field.getType(), fieldTypes)) {
                    fields.add(field);
                }
            }
        }
        return fields;
    }

    public static boolean isAssignableFrom(final Class<?> askedType, final List<Class<?>> fieldTypes) {
        if (fieldTypes == null || fieldTypes.isEmpty()) {
            throw new IllegalArgumentException("The list of types should be non-empty.");
        }
        for (final Class<?> t : fieldTypes) {
            if (t.isAssignableFrom(askedType)) {
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
        final List<Field> fields = new ArrayList<Field>();
        final List<Field> unionProperties = AbstractUnionEntity.unionProperties(type);
        final List<String> commonProperties = AbstractUnionEntity.commonProperties(type);
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
        final List<Field> properties = new ArrayList<Field>();
        Class<?> klass = type;
        if (AbstractUnionEntity.class.isAssignableFrom(klass) && withUnion) {
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
        final List<Field> properties = new ArrayList<Field>();
        final List<Field> keyProps = new ArrayList<>();
        final Set<String> fieldNames = new HashSet<>();
        for (final Field field : wholeHierarchyProperties) {
            if (!fieldNames.contains(field.getName())) {
                fieldNames.add(field.getName());
                if (isKey(field)) {
                    keyProps.add(field);
                } else {
                    properties.add(field);
                }
            }
        }

        // prepend key properties at the beginning of the list of properties
        // this is essential for serialisation to correctly initialise collectional properties
        final List<Field> propertiesWithKeys = new ArrayList<Field>(keyProps);
        propertiesWithKeys.addAll(properties);

        return propertiesWithKeys;
    }

    private static boolean isKey(final Field field) {
        return field.getName().equals(AbstractEntity.KEY) || field.isAnnotationPresent(CompositeKeyMember.class);
    }

    /**
     * Returns a stream of fields (including private, protected and public) annotated with the specified annotation. This method processes the whole class hierarchy.
     *
     * @param type
     * @param annotation
     * @param withUnion
     *            - determines whether include union entitie's properties (i.e. common properties, union properties) or just simple union entity fields.
     *
     * @return
     */
    private static Stream<Field> getFieldsAnnotatedWith(final Class<?> type, final boolean withUnion, final Class<? extends Annotation> annot, final Class<? extends Annotation>... annotations) {
        final Set<Class<? extends Annotation>> allAnnotations = new HashSet<Class<? extends Annotation>>();
        allAnnotations.add(annot);
        allAnnotations.addAll(Arrays.asList(annotations));
        return streamFieldsAnnotatedWith(getFields(type, withUnion), allAnnotations);
    }

    /**
     * Returns value of the {@link AbstractUnionEntity} field specified with property.
     *
     * @param value
     * @param property
     * @return
     * @throws IllegalAccessException
     */
    private static Object getAbstractUnionEntityFieldValue(final AbstractUnionEntity value, final String property) {
        final Optional<Field> field;
        final Object valueToRetrieveFrom;
        final List<String> unionProperties = getFieldNames(AbstractUnionEntity.unionProperties(value.getClass()));
        final List<String> commonProperties = AbstractUnionEntity.commonProperties(value.getClass());

        try {
            if (unionProperties.contains(property)) { // union properties:
                field = Optional.ofNullable(getFieldByName(value.getClass(), property));
                valueToRetrieveFrom = value;
            } else if (commonProperties.contains(property) || COMMON_PROPS.contains(property) || ID.equals(property)) { // common property:
                final AbstractEntity<?> activeEntity = value.activeEntity();
                field = Optional.ofNullable(activeEntity != null ? getFieldByName(activeEntity.getClass(), property) : null);
                valueToRetrieveFrom = activeEntity;
            } else { // not-properly specified property:
                throw new ReflectionException(format("Property [%s] is not properly specified. Maybe \"activeEntity.\" prefix should be explicitly specified.", property));
            }
            return field.isPresent() ? getFieldValue(field.get(), valueToRetrieveFrom) : null;
        } catch (final Exception e) {
            e.printStackTrace();
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
    private static Object getAbstractUnionEntityMethodValue(final AbstractUnionEntity instance, final String methodName, final Class<?>... arguments) throws NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        try {
            final Method method = Reflector.getMethodForClass(instance.getClass(), methodName, arguments);
            return getMethodValue(method, instance);
        } catch (final NoSuchMethodException e) {
            final AbstractEntity activeEntity = instance.activeEntity();
            if (activeEntity != null && AbstractUnionEntity.commonMethodNames((Class<AbstractUnionEntity>) instance.getType()).contains(methodName)) {
                final Method method = Reflector.getMethodForClass(activeEntity.getClass(), methodName, arguments);
                return getMethodValue(method, activeEntity);
            } else {
                throw new RuntimeException("active entity can not be null");
            }
        }
    }

    /**
     * Returns field value for the {@code valueToRetrievefrom} object.
     *
     * @param field
     * @param valueToRetrieveFrom
     * @return
     * @throws IllegalAccessException
     */
    public static Object getFieldValue(final Field field, final Object valueToRetrieveFrom) throws IllegalAccessException {
        final boolean isAccessible = field.isAccessible();
        field.setAccessible(true);
        final Object value = field.get(valueToRetrieveFrom);
        field.setAccessible(isAccessible);
        return value;
    }

    /**
     * Invokes specified method on given {@code objectToInvoceOn}.
     *
     * @param method
     * @param objectToInvokeOn
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private static Object getMethodValue(final Method method, final Object objectToInvokeOn) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        final boolean isAccessible = method.isAccessible();
        method.setAccessible(true);
        final Object value = method.invoke(objectToInvokeOn);
        method.setAccessible(isAccessible);
        return value;
    }

    /**
     * Returns value of the field specified with property parameter.
     *
     * @param value
     * @param property
     * @return
     * @throws IllegalAccessException
     */
    private static Object getPropertyValue(Object value, final String property) throws IllegalAccessException {
        if (!property.contains("()")) {
            if (value instanceof AbstractUnionEntity) {
                value = getAbstractUnionEntityFieldValue((AbstractUnionEntity) value, property);
            } else {
                value = getFieldValue(getFieldByName(value.getClass(), property), value);
            }
        } else {
            try {
                if (value instanceof AbstractUnionEntity) {
                    value = getAbstractUnionEntityMethodValue((AbstractUnionEntity) value, property.substring(0, property.length() - 2));
                } else {
                    value = getMethodValue(Reflector.getMethod(value.getClass(), property.substring(0, property.length() - 2)), value);
                }
            } catch (final NoSuchMethodException e) {
                throw new IllegalArgumentException("Failed to locate parameterless method " + property + " in " + value.getClass(), e);
            } catch (final InvocationTargetException e) {
                throw new IllegalArgumentException("Failed to invoke parameterless method " + property + " on instance of " + value.getClass(), e);
            }
        }
        return value;
    }

    // ========================================================================================================
    /////////////////////////////// Miscellaneous utilities ///////////////////////////////////////////////////

    /**
     * Returns a list of properties that are present in all of the types passed into the method.
     *
     * @param entityTypes
     * @return
     */
    public static List<String> findCommonProperties(final List<Class<? extends AbstractEntity<?>>> entityTypes) {
        final List<List<Field>> propertiesSet = new ArrayList<List<Field>>();
        for (int classIndex = 0; classIndex < entityTypes.size(); classIndex++) {
            final List<Field> fields = new ArrayList<Field>();
            for (final Field propertyField : findProperties(entityTypes.get(classIndex))) {
                fields.add(propertyField);
            }
            propertiesSet.add(fields);
        }
        final List<String> commonProperties = new ArrayList<String>();
        if (propertiesSet.size() > 0) {
            for (final Field property : propertiesSet.get(0)) {
                boolean common = true;
                for (int setIndex = 1; setIndex < propertiesSet.size(); setIndex++) {
                    if (!isPropertyPresent(property, propertiesSet.get(setIndex))) {
                        common = false;
                        break;
                    }
                }
                if (common) {
                    commonProperties.add(property.getName());
                }
            }
        }
        return commonProperties;
    }

    /**
     * Returns true if the field with appropriate name and type is present in the list of specified properties.
     *
     *
     * @param field
     * @param properties
     * @return
     */
    private static boolean isPropertyPresent(final Field field, final List<Field> properties) {
        for (final Field property : properties) {
            // Need a special handle for property key
            if (field.getName().equals(property.getName())) {
                Class<?> fieldType = field.getType();
                Class<?> propertyType = property.getType();
                if (KEY.equals(field.getName())) {
                    final Class<AbstractEntity> fieldOwner = (Class<AbstractEntity>) field.getDeclaringClass();
                    final Class<AbstractEntity> propertyOwner = (Class<AbstractEntity>) property.getDeclaringClass();

                    fieldType = AnnotationReflector.getKeyType(fieldOwner);
                    propertyType = AnnotationReflector.getKeyType(propertyOwner);
                }
                if (fieldType != null && propertyType != null && fieldType.equals(propertyType)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns <code>true</code> if property (defined by <code>dotNotationExp</code>) is present in type <code>forType</code>. Field should have {@link IsProperty} annotation
     * assigned to be recognised as "property".
     *
     * @param forType
     * @param dotNotationExp
     * @return
     */
    public static boolean isPropertyPresent(final Class<?> forType, final String dotNotationExp) {
        try {
            return AnnotationReflector.getPropertyAnnotation(IsProperty.class, forType, dotNotationExp) != null;
        } catch (final IllegalArgumentException iae) {
            return false;
        } catch (final MethodFoundException mfe) {
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
        final List<String> result = new ArrayList<String>();
        for (int index = 0; index < fields.size(); index++) {
            result.add(fields.get(index).getName());
        }
        return result;
    }

    /**
     * A contract for ignoring properties and entity type during composition of property paths.
     *
     */
    public interface IPropertyPathFilteringCondition {
        boolean ignore(String propertyName);

        boolean ignore(Class<?> enttyType);
    }

    /**
     * Marks exceptional situation when method was found by according dot-notation expression.
     *
     * @author TG Team
     *
     */
    public static class MethodFoundException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public MethodFoundException(final String message) {
            super(message);
        }
    }

    /**
     * Method to recursively find properties of the specified type in the provided entity type and build dot notated paths.
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
        final List<String> result = new ArrayList<String>();

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
     * @param dotNotationExp
     * @return
     */
    public static String findLinkProperty(final Class<? extends AbstractEntity<?>> type, final String dotNotationExp) {
        final Field field = Finder.findFieldByName(type, dotNotationExp);
        if (!AnnotationReflector.isAnnotationPresent(field, IsProperty.class)) {
            throw new IllegalArgumentException("Field " + dotNotationExp + " in type " + type.getName() + " is not a property.");
        }

        final IsProperty propAnnotation = AnnotationReflector.getAnnotation(field, IsProperty.class);
        // check if meta-data is present and if so use it
        if (!IsProperty.stubForLinkProperty.equals(propAnnotation.linkProperty())) {
            return propAnnotation.linkProperty();
        }
        // otherwise try to determine link property dynamically based on property type and composite key
        final Class<?> masterType = DynamicEntityClassLoader.getOriginalType(field.getDeclaringClass());
        final Class<?> propType = PropertyTypeDeterminator.determinePropertyType(type, dotNotationExp);
        final boolean collectionalProp = PropertyTypeDeterminator.isCollectional(type, dotNotationExp);

        // to be in association property should be an entity type
        if (!AbstractEntity.class.isAssignableFrom(propType)) {
            throw new IllegalArgumentException("Property " + dotNotationExp + " in type " + type.getName() + " is not an entity (" + propType.getName() + ").");
        }

        // non-collectional properties must have their linkProperty specified explicitly, otherwise they're considered to be Many-to-One
        if (!collectionalProp && !isOne2One_association(type, dotNotationExp)) {
            throw new IllegalStateException("Non-collectional property " + dotNotationExp + " in type " + type.getName() + //
            " represents a Many-to-One association.");
        }

        // first check for a link property amongst key members
        final Pair<Integer, String> keyPair = lookForLinkProperty(masterType, getKeyMembers(propType));
        final Integer matchingKeyMemembersCount = keyPair.getKey(); // the number of matched fields that are key members
        final String keylinkProperty = keyPair.getValue(); // the matched key member name
        // check if a key member was found
        if (matchingKeyMemembersCount == 0) {
            throw new IllegalArgumentException("Property " + dotNotationExp + " in type " + type.getName() + " does not have an appropriate key member of type "
                    + masterType.getName() + ".");
        } else if (matchingKeyMemembersCount > 1) {
            // more than one matching key member means ambiguity
            throw new IllegalArgumentException("Property " + dotNotationExp + " in type " + type.getName() + " has more than one key of type " + masterType.getName() + ".");
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

        return new Pair<Integer, String>(count, linkProperty);
    }

    /**
     * Determines whether specified property is one2many or one2one association.
     * <p>
     * The rule is following : if the type of property contains reference to the type of property parent then return <code>true</code>, otherwise <code>false</code>.
     *
     * @param type
     * @param dotNotationExp
     * @return
     */
    public static boolean isOne2Many_or_One2One_association(final Class<?> type, final String dotNotationExp) {
        return isOne2One_association(type, dotNotationExp) || hasLinkProperty(type, dotNotationExp);
    }

    public static boolean hasLinkProperty(final Class<?> type, final String dotNotationExp) {
        // if it is not one-to-one than may be it is one-to-many
        // for this we should try to identify linkProperty, it it is identifiable then return true, otherwise -- false
        try {
            return !StringUtils.isEmpty(findLinkProperty((Class<? extends AbstractEntity<?>>) type, dotNotationExp));
        } catch (final Exception ex) {
            // exception is possible in various cases of incorrectly constructed associations, which should not be recognised as valid one-to-many
            return false;
        }
    }

    /**
     * Determines whether specified property is one2one association.
     * <p>
     * The rule is following : if the type of property contains the "key" of the type of property parent then return <code>true</code>, otherwise <code>false</code>.
     *
     * @param type
     * @param dotNotationExp
     * @return
     */
    public static boolean isOne2One_association(final Class<?> type, final String dotNotationExp) {
        final Class<?> propertyType = PropertyTypeDeterminator.determinePropertyType(type, dotNotationExp);
        final Class<?> masterType = DynamicEntityClassLoader.getOriginalType(PropertyTypeDeterminator.transform(type, dotNotationExp).getKey());
        return EntityUtils.isEntityType(propertyType)
                && DynamicEntityClassLoader.getOriginalType(PropertyTypeDeterminator.determinePropertyType(propertyType, KEY)).equals(masterType);
    }

}