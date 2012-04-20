package ua.com.fielden.platform.entity.query.fluent;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

public class fetch<T extends AbstractEntity<?>> {
    private final Class<T> entityType;
    private final Map<String, fetch<? extends AbstractEntity<?>>> entityProps = new HashMap<String, fetch<? extends AbstractEntity<?>>>();
    private final Set<String> primProps = new HashSet<String>();


    /**
     * Used mainly for serialisation.
     */
    protected fetch() {
	this.entityType = null;
    }

    protected fetch(final Class<T> entityType) {
	this.entityType = entityType;
	enhanceFetchModelWithKeyProperties();
    }

    private void enhanceFetchModelWithKeyProperties() {
	final List<String> keyMemberNames = Finder.getFieldNames(Finder.getKeyMembers(entityType));
	for (final String keyProperty : keyMemberNames) {
	    with(keyProperty);
	}
    }

    protected void withAll() {
	final List<Field> fields = Finder.findPropertiesThatAreEntities(entityType);
	for (final Field field : fields) {
	    entityProps.put(field.getName(), new fetch(field.getType()));
	}
    }

    private Class getPropType(final String propName) {
	try {
	    return PropertyTypeDeterminator.determinePropertyType(entityType, propName);
	} catch (final Exception e) {
	    throw new IllegalArgumentException("Trying fetch entity of type [" + entityType + "] with non-existing property [" + propName + "]");
	}
    }

    public fetch<T> with(final String propName) {
	final Class propType = getPropType(propName);
	if (AbstractEntity.class.isAssignableFrom(propType)) {
	    entityProps.put(propName, new fetch(propType));
	} else {
	    primProps.add(propName);
	}
	return this;
    }

    public fetch<T> with(final String propName, final fetch<? extends AbstractEntity<?>> fetchModel) {
	if (entityType != EntityAggregates.class) {
	    final Class propType = getPropType(propName);

	    if (propType != fetchModel.entityType) {
		throw new IllegalArgumentException("Mismatch between actual type of property and its fetch model type!");
	    }
	}

	if (AbstractEntity.class.isAssignableFrom(fetchModel.getEntityType())) {
	    entityProps.put(propName, fetchModel);
	} else {
	    throw new IllegalArgumentException(propName + " has fetch model for type " + fetchModel.getEntityType().getName() + ". Fetch model with entity type is required.");
	}
	return this;
    }

    public Map<String, fetch<? extends AbstractEntity<?>>> getFetchModels() {
	return entityProps;
    }

    public Class<T> getEntityType() {
	return entityType;
    }

    @Override
    public String toString() {
	return getString("     ");
    }

    private String getString(final String offset) {
	final StringBuffer sb = new StringBuffer();
	sb.append("\n");
	for (final Map.Entry<String, fetch<?>> fetchModel : entityProps.entrySet()) {
	    sb.append(offset + fetchModel.getKey() + fetchModel.getValue().getString(offset + "   "));
	}

	return sb.toString();
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((entityType == null) ? 0 : entityType.hashCode());
	result = prime * result + ((entityProps == null) ? 0 : entityProps.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (!(obj instanceof fetch)) {
	    return false;
	}

	final fetch that = (fetch) obj;
	if (entityType == null) {
	    if (that.entityType != null) {
		return false;
	    }
	} else if (!entityType.equals(that.entityType)) {
	    return false;
	}
	if (entityProps == null) {
	    if (that.entityProps != null) {
		return false;
	    }
	} else if (!entityProps.equals(that.entityProps)) {
	    return false;
	}
	return true;
    }
}