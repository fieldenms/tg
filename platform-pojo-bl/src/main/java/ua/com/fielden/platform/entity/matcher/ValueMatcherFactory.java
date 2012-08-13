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
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;

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
    public IValueMatcher<?> getValueMatcher(final Class<? extends AbstractEntity<?>> propertyOwnerEntityType, final String propertyName) {
	if (propertyOwnerEntityType == null) {
	    throw new IllegalArgumentException("A valid entity type is expected.");
	}
	final Map<String, IValueMatcher> entityEntry = getEntityMap(propertyOwnerEntityType);
	final IValueMatcher matcher = entityEntry.get(propertyName);
	return matcher != null ? matcher : createMatcher(propertyOwnerEntityType, propertyName, entityEntry);
    }

    public EntityFactory getEntityFactory() {
	return entityFactory;
    }

    /**
     * Instantiates a value matcher based on the passed parameters, caches it and return is method's result.
     *
     * @param propertyOwnerEntityType
     * @param propertyName
     * @param entityEntry
     * @param additionalParameters
     * @return
     */
    private IValueMatcher createMatcher(final Class<? extends AbstractEntity<?>> propertyOwnerEntityType, final String propertyName, final Map<String, IValueMatcher> entityEntry) {
	if (entityEntry.get(propertyName) == null) {
	    final Field propField = Finder.findFieldByName(propertyOwnerEntityType, propertyName);
	    final Class<?> propType = getPropertyType(propertyOwnerEntityType, propField);
	    // instantiate value matcher based on the entity type
	    if (isOwnerACriteria(propertyOwnerEntityType)) { // criteria entity
		createMatcherForCriteriaEntity(propertyOwnerEntityType, propertyName, entityEntry, propField, propType);
	    } else { // ordinary domain entity
		createMathcerForDomainEntity(propertyOwnerEntityType, propertyName, entityEntry, propType);
	    }

	}
	return entityEntry.get(propertyName);
    }

    /** Instantiates a matcher for a property of an entity representing criteria. */
    private void createMatcherForCriteriaEntity(final Class<? extends AbstractEntity<?>> propertyOwnerEntityType, final String propertyName, final Map<String, IValueMatcher> entityEntry, final Field propField, final Class<?> propType) {
	if (isPropertyAnEntity(propType)) { // this is an unusual case since most criteria are multi-valued
	    if (PropertyDescriptor.class.isAssignableFrom(propType)) {
		createPropertyDescriptorMatcher(propertyOwnerEntityType, propertyName, entityEntry);
	    } else {
		entityEntry.put(propertyName, new EntityQueryValueMatcher(daoFactory.newDao((Class<AbstractEntity<?>>) propType), "key"));
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
		final Class<? extends AbstractEntity<?>> notEnhancedElType = (Class<AbstractEntity<?>>)DynamicEntityClassLoader.getOriginalType(elType.value());
		if (keyType != null && AbstractEntity.class.isAssignableFrom(keyType)) {
		    entityEntry.put(propertyName, new EntityQueryValueMatcher(daoFactory.newDao(notEnhancedElType), "key.key", "key.key"));
		} else {
		    entityEntry.put(propertyName, new EntityQueryValueMatcher(daoFactory.newDao(notEnhancedElType), "key"));
		}
	    }
	} else if (propType.isEnum()) {
	    entityEntry.put(propertyName, new EnumValueMatcher(propType));
	} else {
	    throw new IllegalArgumentException("Criteria " + propertyOwnerEntityType.getName() + ": " + propType.getName()
		    + " is not a valid property type and thus cannot have a value matcher.");
	}
    }

    /** Instantiates a matcher for a property of an ordinary domain entity. */
    private void createMathcerForDomainEntity(final Class<? extends AbstractEntity<?>> propertyOwnerEntityType, final String propertyName, final Map<String, IValueMatcher> entityEntry, final Class<?> propType) {
	if (propType.isEnum()) {
	    entityEntry.put(propertyName, new EnumValueMatcher(propType));
	} else if (!isPropertyAnEntity(propType)) {
	    throw new IllegalArgumentException("Entity " + propertyOwnerEntityType.getName() + ": property " + propertyName + " of type " + propType.getName()
		    + " is not a valid property type and thus cannot have a value matcher.");
	}

	final Class<?> keyType = AnnotationReflector.getKeyType(propType);
	if (keyType != null && AbstractEntity.class.isAssignableFrom(keyType)) {
	    entityEntry.put(propertyName, new EntityQueryValueMatcher(daoFactory.newDao((Class<? extends AbstractEntity<?>>) propType), "key.key", "key.key"));
	} else if (PropertyDescriptor.class.isAssignableFrom(propType)) {
	    createPropertyDescriptorMatcher(propertyOwnerEntityType, propertyName, entityEntry);
	} else {
	    entityEntry.put(propertyName, new EntityQueryValueMatcher(daoFactory.newDao((Class<? extends AbstractEntity<?>>) propType), "key"));
	}
    }

    private void createPropertyDescriptorMatcher(final Class<? extends AbstractEntity<?>> propertyOwnerEntityType, final String propertyName, final Map<String, IValueMatcher> entityEntry) {
	final Class<? extends AbstractEntity<?>> type = (Class<? extends AbstractEntity<?>>) AnnotationReflector.getPropertyAnnotation(IsProperty.class, propertyOwnerEntityType, propertyName).value();
	final List<?> values = entityFactory != null ? Finder.getPropertyDescriptors(type, entityFactory) : Finder.getPropertyDescriptors(type);
	entityEntry.put(propertyName, new PojoValueMatcher(values, "key", values.size())); // instead of a key there could be propertyName
    }

    /** Creates value matcher for a collection of property descriptors. Usually used for building criteria. */
    private void createPropertyDescriptorMatcherForCollection(final Class<? extends AbstractEntity<?>> propertyOwnerEntityType, final String propertyName, final Map<String, IValueMatcher> entityEntry) {
	final Class<? extends AbstractEntity> type = AnnotationReflector.getPropertyAnnotation(EntityType.class, propertyOwnerEntityType, propertyName).value();
	final List<?> values = entityFactory != null ? Finder.getPropertyDescriptors(type, entityFactory) : Finder.getPropertyDescriptors(type);
	entityEntry.put(propertyName, new PojoValueMatcher(values, "key", values.size())); // instead of a key there could be propertyName
    }

    private Map<String, IValueMatcher> getEntityMap(final Class<? extends AbstractEntity<?>> propertyOwnerEntityType) {
	Map<String, IValueMatcher> entityEntry = map.get(isOwnerACriteria(propertyOwnerEntityType) ? EntityQueryCriteria.class : propertyOwnerEntityType);
	if (entityEntry == null) {
	    entityEntry = new HashMap<String, IValueMatcher>();
	    map.put(propertyOwnerEntityType, entityEntry);
	}
	return entityEntry;
    }

    /**
     * Determines whether specified class is criteria class or not.
     *
     * @param propertyOwnerEntityType
     * @return
     */
    private boolean isOwnerACriteria(final Class<? extends AbstractEntity> propertyOwnerEntityType) {
	return EntityQueryCriteria.class.isAssignableFrom(propertyOwnerEntityType);
    }

    /**
     * Determines whether specified class is entity class or not.
     *
     * @param propertyOwnerEntityType
     * @return
     */
    private boolean isPropertyAnEntity(final Class<?> propertyType) {
	return AbstractEntity.class.isAssignableFrom(propertyType);
    }

    /**
     * Returns the type of specified property in the propertOwnerEntityType class.
     *
     * @param propertyOwnerEntityType
     * @param propertyField
     * @return
     */
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
}
