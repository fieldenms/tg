package ua.com.fielden.platform.entity.query;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

public class fetch<T extends AbstractEntity<?>> {
    private final Class<T> entityType;
    private final Map<String, fetch<? extends AbstractEntity<?>>> fetchModels = new HashMap<String, fetch<? extends AbstractEntity<?>>>();
    private final List<String> fetchedProps = new ArrayList<String>();

    /**
     * Used mainly for serialisation.
     */
    protected fetch() {
	this.entityType = null;
    }

    public fetch(final Class<T> entityType) {
	this.entityType = entityType;
	enhanceFetchModelWithKeyProperties();
    }

    private void enhanceFetchModelWithKeyProperties() {
	final List<String> keyMemberNames = Finder.getFieldNames(Finder.getKeyMembers(entityType));
	for (final String keyProperty : keyMemberNames) {
	    final Class propType = PropertyTypeDeterminator.determinePropertyType(entityType, keyProperty);
	    if (AbstractEntity.class.isAssignableFrom(propType)) {
		with(keyProperty, new fetch(propType));
	    }
	}
    }

    protected void withAll() {
	final List<Field> fields = Finder.findPropertiesThatAreEntities(entityType);
	for (final Field field : fields) {
	    fetchModels.put(field.getName(), new fetch(field.getType()));
	}
    }

    public fetch<T> with(final String propName) {
	final Class propType = PropertyTypeDeterminator.determinePropertyType(entityType, propName);
	if (AbstractEntity.class.isAssignableFrom(propType)) {
	    fetchModels.put(propName, new fetch(propType));
	} else {
	    throw new IllegalArgumentException(propName + " is of type " + propType.getName() + ". Only property, which is entity can be fetched");
	}
	return this;
    }

    public fetch<T> with(final String propName, final fetch<? extends AbstractEntity<?>> fetchModel) {
	if (AbstractEntity.class.isAssignableFrom(fetchModel.getEntityType())) {
	    fetchModels.put(propName, fetchModel);
	} else {
	    throw new IllegalArgumentException(propName + " has fetch model for type " + fetchModel.getEntityType().getName() + ". Fetch model with entity type is required.");
	}
	return this;
    }

    public Map<String, fetch<? extends AbstractEntity<?>>> getFetchModels() {
	return fetchModels;
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
	for (final Map.Entry<String, fetch<?>> fetchModel : fetchModels.entrySet()) {
	    sb.append(offset + fetchModel.getKey() + fetchModel.getValue().getString(offset + "   "));
	}

	return sb.toString();
    }
}