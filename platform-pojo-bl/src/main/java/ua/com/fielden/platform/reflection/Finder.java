package ua.com.fielden.platform.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Monitoring;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.treemodel.IPropertyFilter;
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
	MetaProperty result = null;
	for (final String propertyName : properties) {
	    // if the owner is null then there is no way it is possible to determine the meta-property.
	    if (!(owner instanceof AbstractEntity)) {
		// throw new RuntimeException("The property " + propertyName + " owner is null.");
		break;
	    }
	    // get the meta-property instance, which can but should not be null
	    result = ((AbstractEntity<?>) owner).getProperty(propertyName);
	    if (result != null) {
		metaProperties.add(result);
	    } else {
		throw new IllegalArgumentException("Failed to locate meta-property " + dotNotationExp + " starting with entity " + entity.getType() + ": " + entity);
	    }
	    // obtain the value for the current propertyName, which becomes the owner for the next property
	    owner = ((AbstractEntity<?>) owner).get(propertyName);
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
    public static SortedSet<MetaProperty> getMetaProperties(final AbstractEntity<?> entity) {
	final List<Field> properties = findRealProperties(entity.getType());
	final SortedSet<MetaProperty> metaProperties = new TreeSet<MetaProperty>();
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
    public static List<MetaProperty> getCollectionalMetaProperties(final AbstractEntity<?> entity, final Class<?> collectionType) {
	final SortedSet<MetaProperty> metaProperties = getMetaProperties(entity);
	final List<MetaProperty> collectional = new ArrayList<MetaProperty>();
	for (final MetaProperty metaProperty : metaProperties) {
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
    public static List<Field> findProperties(final Class<?> entityType, final Class<? extends Annotation>... annotations) {
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
    public static List<Field> findRealProperties(final Class<?> entityType, final Class<? extends Annotation>... annotations) {
        return getFieldsAnnotatedWith(entityType, false, IsProperty.class, annotations);
    }

    /**
     * Returns the list of properties that could be used in lifecycle reporting.
     *
     * @return
     */
    public static List<Field> findLifecycleProperties(final Class<? extends AbstractEntity> clazz) {
	final List<Field> properties = findProperties(clazz, Monitoring.class);
	properties.remove(getFieldByName(clazz, AbstractEntity.KEY));
	properties.remove(getFieldByName(clazz, AbstractEntity.DESC));
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
     * Determines properties within the provided class to be used for a key. There are two cases: either entity uses a composite key or a single property <code>key</code> represent
     * a key.
     * <p>
     * The implementation of this method is based on {@link #getFieldsAnnotatedWith(Class, Class)}, which traverses the whole class hierarchy. Thus, it supports correct
     * determination of properties declared at different hierarchical levels constituting a part of the composite key.
     *
     * @param klass
     * @return
     */
    public static List<Field> getKeyMembers(final Class<?> type) {
        final SortedMap<Integer, Field> properties = new TreeMap<Integer, Field>(); // the use of SortedMap ensures the correct order of properties to be used the composite key
        final List<Field> compositeKeyFields = findRealProperties(type, CompositeKeyMember.class);

        for (final Field field : compositeKeyFields) {
            final CompositeKeyMember annotation = field.getAnnotation(CompositeKeyMember.class);
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
            keyMembers.add(getFieldByName(type, AbstractEntity.KEY));
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
    public static Object findFieldValueByName(final Object instance, final String dotNotationExp) throws Exception {
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
        return value;
    }

    //    /**
    //     * An alternative implementation to the above method using tail recursion calls instead of iteration.
    //     *
    //     * @param instance
    //     * @param dotNotationExp
    //     * @return
    //     * @throws Exception
    //     */
    //    public static Object findFieldValueByNameRec(final Object instance, final String dotNotationExp) throws Exception {
    //        return instance == null ? null : // if instance if null then return null
    //        	// -- cannot continue
    //        	!dotNotationExp.contains(".") ? // if the dot notation is a
    //        	// simple property name then
    //        	// return its value
    //        	getPropertyValue(instance, dotNotationExp)
    //        		: // otherwise recursively traverse the dot expression
    //        		findFieldValueByNameRec(getPropertyValue(instance, dotNotationExp.substring(0, dotNotationExp.indexOf("."))), dotNotationExp.substring(dotNotationExp.indexOf(".") + 1));
    //    }

    /**
     * Searches through the owner type hierarchy for all fields of the type assignable to the provided field type.
     *
     * @param ownerType
     * @param fieldType
     * @return list of found fields, which can be empty
     */
    public static List<Field> getFieldsOfSpecifiedType(final Class<?> ownerType, final Class<?> fieldType) {
	return getFieldsOfSpecifiedTypes(ownerType, Arrays.<Class<?>>asList(fieldType));
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
     * Traces through specified list of fields and returns those annotated with allAnnotations.
     *
     * @param fields
     * @param allAnnotations
     * @return
     */
    private static List<Field> getFieldsAnnotatedWith(final List<Field> fields, final Collection<Class<? extends Annotation>> allAnnotations) {
        final List<Field> properties = new ArrayList<Field>();
        for (final Field field : fields) {
            int count = 0;
            for (final Class<? extends Annotation> annotation : allAnnotations) {
        	if (field.isAnnotationPresent(annotation)) {
        	    count++;
        	}
            }
            if (count == allAnnotations.size()) {
        	properties.add(field);
            }
        }
        return properties;
    }

    /**
     * Returns a list of fields (including private, protected and public). This method processes the whole class hierarchy.
     *
     * @param type
     * @param withUnion - determines whether include union entitie's properties (i.e. common properties, union properties) or just simple union entity fields.
     * @return
     */
    public static List<Field> getFields(final Class<?> type, final boolean withUnion){
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
	return properties;
    }

    /**
     * Returns a list of fields (including private, protected and public) annotated with the specified annotation. This method processes the whole class hierarchy.
     *
     * @param type
     * @param annotation
     * @param withUnion - determines whether include union entitie's properties (i.e. common properties, union properties) or just simple union entity fields.
     *
     * @return
     */
    private static List<Field> getFieldsAnnotatedWith(final Class<?> type, final boolean withUnion, final Class<? extends Annotation> annot, final Class<? extends Annotation>... annotations) {
        final Set<Class<? extends Annotation>> allAnnotations = new HashSet<Class<? extends Annotation>>();
        allAnnotations.add(annot);
        allAnnotations.addAll(Arrays.asList(annotations));
        return getFieldsAnnotatedWith(getFields(type, withUnion), allAnnotations);
//        final List<Field> properties = new ArrayList<Field>();
//        Class<?> klass = type;
//        if (AbstractUnionEntity.class.isAssignableFrom(klass) && withUnion) {
//            properties.addAll(getFieldsAnnotatedWith(getUnionEntityFields((Class<AbstractUnionEntity>) type), allAnnotations));
//        } else {
//            while (klass != Object.class) { // need to iterated thought
//        	// hierarchy in order to retrieve
//        	// fields from above the current
//        	// instance
//        	// iterate though the list of fields declared in the class
//        	// represented by klass variable, and add those annotated with
//        	// the specified annotation
//        	properties.addAll(getFieldsAnnotatedWith(Arrays.asList(klass.getDeclaredFields()), allAnnotations));
//        	// move to the upper class in the hierarchy in search for more
//        	// fields
//        	klass = klass.getSuperclass();
//            }
//        }
//        return properties;
    }

    /**
     * Returns value of the {@link AbstractUnionEntity} field specified with property.
     *
     * @param value
     * @param property
     * @return
     * @throws IllegalAccessException
     */
    private static Object getAbstractUnionEntityFieldValue(final AbstractUnionEntity value, final String property) throws IllegalAccessException {
        Field field = null;
        Object valueToRetrieveFrom = null;
        final List<String> unionProperties = getFieldNames(AbstractUnionEntity.unionProperties(value.getClass()));
        final List<String> commonProperties = AbstractUnionEntity.commonProperties(value.getClass());

        try {
            if (unionProperties.contains(property)) { // union properties:
        	field = getFieldByName(value.getClass(), property);
        	valueToRetrieveFrom = value;
            } else if (commonProperties.contains(property) || AbstractEntity.KEY.equals(property) || AbstractEntity.ID.equals(property) || AbstractEntity.DESC.equals(property)) { // common property:
        	final AbstractEntity<?> activeEntity = value.activeEntity();
        	field = getFieldByName(activeEntity.getClass(), property);
        	valueToRetrieveFrom = activeEntity;
            } else { // not-properly specified property:
        	throw new RuntimeException("Property [" + property + "] is not properly specified. Maybe \"activeEntity.\" prefix should be explicitly specified.");
            }
            return getFieldValue(field, valueToRetrieveFrom);
        } catch (final Exception e) {
            throw new RuntimeException("Property [" + property + "] is not properly specified. Maybe \"activeEntity.\" prefix should be explicitly specified.");
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
    public static List<String> findCommonProperties(final List<Class<? extends AbstractEntity>> entityTypes, final IPropertyFilter propertyFilter) {
        final List<List<Field>> propertiesSet = new ArrayList<List<Field>>();
        for (int classIndex = 0; classIndex < entityTypes.size(); classIndex++) {
            final List<Field> fields = new ArrayList<Field>();
            for (final Field propertyField : findProperties(entityTypes.get(classIndex))) {
        	if (propertyFilter == null || !propertyFilter.shouldExcludeProperty(entityTypes.get(classIndex), propertyField)) {
        	    fields.add(propertyField);
        	}
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
        	if (AbstractEntity.KEY.equals(field.getName())) {
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
     * Returns <code>true</code> if property (defined by <code>dotNotationExp</code>) is present in type <code>forType</code>.
     * Field should have {@link IsProperty} annotation assigned to be recognised as "property".
     *
     * @param forType
     * @param dotNotationExp
     * @return
     */
    public static <T extends Annotation> boolean isPropertyPresent(final Class<?> forType, final String dotNotationExp) {
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
}
