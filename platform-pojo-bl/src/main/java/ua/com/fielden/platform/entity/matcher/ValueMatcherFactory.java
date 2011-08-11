package ua.com.fielden.platform.entity.matcher;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.basic.autocompleter.EntityQueryValueMatcher;
import ua.com.fielden.platform.basic.autocompleter.EnumValueMatcher;
import ua.com.fielden.platform.basic.autocompleter.PojoValueMatcher;
import ua.com.fielden.platform.dao.IDaoFactory;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.swing.review.DynamicEntityQueryCriteria;
import ua.com.fielden.platform.swing.review.DynamicProperty;
import ua.com.fielden.platform.swing.review.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * Provides a generic implementation based on IDaoFactory (thus supporting DAO and RAO) for instantiation of value matchers based on the type of concrete properties.
 * 
 * @author TG Team
 * 
 */
public class ValueMatcherFactory implements IValueMatcherFactory {
    private final Map<Class, Map<String, IValueMatcher>> map = new HashMap<Class, Map<String, IValueMatcher>>();
    private final IDaoFactory daoFactory;
    private final EntityFactory entityFactory;

    @Inject
    public ValueMatcherFactory(final IDaoFactory daoFactory, final EntityFactory entityFactory) {
	this.daoFactory = daoFactory;
	this.entityFactory = entityFactory;
    }

    @Override
    public IValueMatcher<?> getValueMatcher(final Class<? extends AbstractEntity> propertyOwnerEntityType, final String propertyName, final Object... additionalParameters) {
	if (propertyOwnerEntityType == null) {
	    throw new IllegalArgumentException("A valid entity type is expected.");
	}
	final Map<String, IValueMatcher> entityEntry = map.get(propertyOwnerEntityType);
	if (entityEntry != null) {
	    final IValueMatcher matcher = entityEntry.get(propertyName);
	    return matcher != null ? matcher : createMatcher(propertyOwnerEntityType, propertyName, additionalParameters);
	}
	return createMatcher(propertyOwnerEntityType, propertyName, additionalParameters);
    }

    /**
     * Instantiates a value matcher based on the passed parameters, caches it and return is method's result.
     * 
     * @param propertyOwnerEntityType
     * @param propertyName
     * @param additionalParameters
     * @return
     */
    private IValueMatcher createMatcher(final Class<? extends AbstractEntity> propertyOwnerEntityType, final String propertyName, final Object... additionalParameters) {
	final Map<String, IValueMatcher> entityEntry = getEntityMap(propertyOwnerEntityType);
	if (entityEntry.get(propertyName) == null) {
	    final Field propField = DynamicEntityQueryCriteria.class.isAssignableFrom(propertyOwnerEntityType) ? null
		    : Finder.findFieldByName(propertyOwnerEntityType, propertyName);
	    final Class<?> propType = getPropertyType(propertyOwnerEntityType, propField);
	    // instantiate value matcher based on the entity type
	    if (!isOwnerACriteria(propertyOwnerEntityType)) { // ordinary domain entity
		createMathcerForDomainEntity(propertyOwnerEntityType, propertyName, entityEntry, propType);
	    } else if (isOwnerADynamicCriteria(propertyOwnerEntityType)) { // dynamic criteria entity
		createMatcherForDynamicCriteriaEntity(propertyOwnerEntityType, propertyName, entityEntry, additionalParameters);
	    } else { // criteria entity
		createMatcherForCriteriaEntity(propertyOwnerEntityType, propertyName, entityEntry, propField, propType);
	    }

	}
	return entityEntry.get(propertyName);
    }

    /** Instantiates a matcher for a property of an entity representing criteria. */
    private void createMatcherForCriteriaEntity(final Class<? extends AbstractEntity> propertyOwnerEntityType, final String propertyName, final Map<String, IValueMatcher> entityEntry, final Field propField, final Class<?> propType) {
	if (isPropertyAnEntity(propType)) { // this is an unusual case since most criteria are multi-valued
	    if (PropertyDescriptor.class.isAssignableFrom(propType)) {
		createPropertyDescriptorMatcher(propertyOwnerEntityType, propertyName, entityEntry);
	    } else {
		entityEntry.put(propertyName, new EntityQueryValueMatcher(daoFactory.newDao((Class<AbstractEntity>) propType), "key"));
	    }
	} else if (propField.isAnnotationPresent(EntityType.class)) {
	    final EntityType elType = propField.getAnnotation(EntityType.class);
	    if (elType.value().isEnum()) {
		entityEntry.put(propertyName, new EnumValueMatcher(elType.value()));
	    } else if (!isPropertyAnEntity(elType.value())) {
		throw new IllegalArgumentException("Criteria " + propertyOwnerEntityType.getName() + ": element " + elType.value().getName()
			+ " is not a valid property type and thus cannot have a value matcher.");
	    }

	    if (PropertyDescriptor.class.isAssignableFrom(propField.getAnnotation(IsProperty.class).value())) {
		createPropertyDescriptorMatcherForCollection(propertyOwnerEntityType, propertyName, entityEntry);
	    } else {
		final Class<?> keyType = AnnotationReflector.getKeyType(elType.value());
		if (keyType != null && AbstractEntity.class.isAssignableFrom(keyType)) {
		    entityEntry.put(propertyName, new EntityQueryValueMatcher(daoFactory.newDao(elType.value()), "key.key", "key.key"));
		} else {
		    entityEntry.put(propertyName, new EntityQueryValueMatcher(daoFactory.newDao(elType.value()), "key"));
		}
	    }
	} else if (propType.isEnum()) {
	    entityEntry.put(propertyName, new EnumValueMatcher(propType));
	} else {
	    throw new IllegalArgumentException("Criteria " + propertyOwnerEntityType.getName() + ": " + propType.getName()
		    + " is not a valid property type and thus cannot have a value matcher.");
	}
    }

    /** Instantiates a matcher for a property of an entity representing dynamic criteria. */
    private void createMatcherForDynamicCriteriaEntity(final Class<? extends AbstractEntity> propertyOwnerEntityType, final String propertyName, final Map<String, IValueMatcher> entityEntry, final Object... additionalParameters) {
	final DynamicEntityQueryCriteria<?, ?> criteria;
	if (additionalParameters.length > 0) {
	    if (additionalParameters[0] instanceof DynamicEntityQueryCriteria) {
		criteria = (DynamicEntityQueryCriteria) additionalParameters[0];
	    } else {
		throw new IllegalArgumentException("first element of the additionalParameters array must be of DynamicEntityQueryCriteria type");
	    }
	} else {
	    throw new IllegalArgumentException("additionalParameters array must have at least one argument of DynamicEntityQueryCriteria type");
	}
	final DynamicProperty<?> dynamicProperty = criteria.getEditableProperty(propertyName);

	if (dynamicProperty.getType().isEnum()) {
	    entityEntry.put(propertyName, new EnumValueMatcher(dynamicProperty.getType()));
	} else if (!isPropertyAnEntity(dynamicProperty.getType())) {
	    throw new IllegalArgumentException("Entity " + propertyOwnerEntityType.getName() + ": property " + propertyName + " of type " + dynamicProperty.getType()
		    + " is not a valid property type and thus cannot have a value matcher.");
	}
	final Class<?> keyType = AnnotationReflector.getKeyType(dynamicProperty.getType());
	if (keyType != null && AbstractEntity.class.isAssignableFrom(keyType)) {
	    entityEntry.put(propertyName, new EntityQueryValueMatcher(daoFactory.newDao((Class<? extends AbstractEntity>) dynamicProperty.getType()), "key.key", "key.key"));
	} else if (PropertyDescriptor.class.isAssignableFrom(dynamicProperty.getType())) {
	    throw new UnsupportedOperationException("PropertyDescriptor is not supported for dynamic properties as this stage.");
	} else {
	    entityEntry.put(propertyName, new EntityQueryValueMatcher(daoFactory.newDao((Class<? extends AbstractEntity>) dynamicProperty.getType()), "key", "key"));
	}
    }

    /** Instantiates a matcher for a property of an ordinary domain entity. */
    private void createMathcerForDomainEntity(final Class<? extends AbstractEntity> propertyOwnerEntityType, final String propertyName, final Map<String, IValueMatcher> entityEntry, final Class<?> propType) {
	if (propType.isEnum()) {
	    entityEntry.put(propertyName, new EnumValueMatcher(propType));
	} else if (!isPropertyAnEntity(propType)) {
	    throw new IllegalArgumentException("Entity " + propertyOwnerEntityType.getName() + ": property " + propertyName + " of type " + propType.getName()
		    + " is not a valid property type and thus cannot have a value matcher.");
	}

	final Class<?> keyType = AnnotationReflector.getKeyType(propType);
	if (keyType != null && AbstractEntity.class.isAssignableFrom(keyType)) {
	    entityEntry.put(propertyName, new EntityQueryValueMatcher(daoFactory.newDao((Class<AbstractEntity>) propType), "key.key", "key.key"));
	} else if (PropertyDescriptor.class.isAssignableFrom(propType)) {
	    createPropertyDescriptorMatcher(propertyOwnerEntityType, propertyName, entityEntry);
	} else {
	    entityEntry.put(propertyName, new EntityQueryValueMatcher(daoFactory.newDao((Class<AbstractEntity>) propType), "key"));
	}
    }

    private void createPropertyDescriptorMatcher(final Class<? extends AbstractEntity> propertyOwnerEntityType, final String propertyName, final Map<String, IValueMatcher> entityEntry) {
	final Class<? extends AbstractEntity> type = (Class<? extends AbstractEntity>) AnnotationReflector.getPropertyAnnotation(IsProperty.class, propertyOwnerEntityType, propertyName).value();
	final List<?> values = entityFactory != null ? Finder.getPropertyDescriptors(type, entityFactory) : Finder.getPropertyDescriptors(type);
	entityEntry.put(propertyName, new PojoValueMatcher(values, "key", values.size())); // instead of a key there could be propertyName
    }

    /** Creates value matcher for a collection of property descriptors. Usually used for building criteria. */
    private void createPropertyDescriptorMatcherForCollection(final Class<? extends AbstractEntity> propertyOwnerEntityType, final String propertyName, final Map<String, IValueMatcher> entityEntry) {
	final Class<? extends AbstractEntity> type = AnnotationReflector.getPropertyAnnotation(EntityType.class, propertyOwnerEntityType, propertyName).value();
	final List<?> values = entityFactory != null ? Finder.getPropertyDescriptors(type, entityFactory) : Finder.getPropertyDescriptors(type);
	entityEntry.put(propertyName, new PojoValueMatcher(values, "key", values.size())); // instead of a key there could be propertyName
    }

    private boolean isOwnerADynamicCriteria(final Class<?> propertyOwnerEntityType) {
	return DynamicEntityQueryCriteria.class.isAssignableFrom(propertyOwnerEntityType);
    }

    private Map<String, IValueMatcher> getEntityMap(final Class<? extends AbstractEntity> propertyOwnerEntityType) {
	Map<String, IValueMatcher> entityEntry = map.get(propertyOwnerEntityType);
	if (entityEntry == null) {
	    entityEntry = new HashMap<String, IValueMatcher>();
	    map.put(propertyOwnerEntityType, entityEntry);
	}
	return entityEntry;
    }

    private boolean isOwnerACriteria(final Class<? extends AbstractEntity> propertyOwnerEntityType) {
	return EntityQueryCriteria.class.isAssignableFrom(propertyOwnerEntityType);
    }

    private boolean isPropertyAnEntity(final Class<?> propertyType) {
	return AbstractEntity.class.isAssignableFrom(propertyType);
    }

    private Class<?> getPropertyType(final Class<? extends AbstractEntity> propertyOwnerEntityType, final Field propertyField) {
	if (propertyField == null) {
	    return null;
	}
	if ("key".equals(propertyField.getName())) {
	    System.out.println("VALUE MATCHER: " + propertyOwnerEntityType.getName());
	    System.out.println("\t\tKEY TYPE: " + AnnotationReflector.getKeyType(propertyOwnerEntityType));
	}
	return "key".equals(propertyField.getName()) ? //
	AnnotationReflector.getKeyType(propertyOwnerEntityType)
		: //
		propertyField.getType();
    }

    public EntityFactory getEntityFactory() {
	return entityFactory;
    }
}
