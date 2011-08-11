package ua.com.fielden.platform.equery;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

public class fetch<T extends AbstractEntity> {
    private final Class<T> entityType;
    private final Map<String, fetch> fetchModels = new HashMap<String, fetch>();


    /**
     * Used mainly for serialisation.
     */
    protected fetch() {
	this.entityType = null;
    }


    public fetch(final Class<T> entityType) {
	this.entityType = entityType;
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

    public fetch<T> with(final String propName, final fetch fetchModel) {
	//System.out.println(fetchModel.getEntityType());
	if (AbstractEntity.class.isAssignableFrom(fetchModel.getEntityType())) {
	    fetchModels.put(propName, fetchModel);
	} else {
	    throw new IllegalArgumentException(propName + " has fetch model for type " + fetchModel.getEntityType().getName() + ". Fetch model with entity type is required.");
	}
	return this;
    }

    public Map<String, fetch> getFetchModels() {
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
	for (final Map.Entry<String, fetch> fetchModel : fetchModels.entrySet()) {
	    sb.append(offset + fetchModel.getKey() + fetchModel.getValue().getString(offset + "   "));
	}

	return sb.toString();
    }
}