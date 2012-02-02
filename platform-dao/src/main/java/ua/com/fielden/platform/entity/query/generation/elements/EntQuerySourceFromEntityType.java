package ua.com.fielden.platform.entity.query.generation.elements;

import java.lang.reflect.Field;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public class EntQuerySourceFromEntityType extends AbstractEntQuerySource {
    private final Class<? extends AbstractEntity> entityType;
    private final boolean generated;

    public EntQuerySourceFromEntityType(final Class<? extends AbstractEntity> entityType, final String alias) {
    	this(entityType, alias, false);
    }

    public EntQuerySourceFromEntityType(final Class<? extends AbstractEntity> entityType, final String alias, final boolean generated) {
	super(alias);
	this.entityType = entityType;
	this.generated = generated;
	generateSourceItems();
    }

    private void generateSourceItems() {
	for (final Field propField : EntityUtils.getPersistedProperties(entityType)) {
	    if (propField.getName().equals("id") || propField.getName().equals("version")) {
		sourceColumns.put(propField.getName(), "_" + propField.getName());
	    } else {
		sourceColumns.put(propField.getName(), propField.getName() + "_");
	    }
	}
    }

    @Override
    public boolean generated() {
	return generated;
    }

    @Override
    public Class sourceType() {
	return entityType;
    }

    @Override
    public String toString() {
        return entityType.getSimpleName() + "-table AS " + getAlias() + " /*GEN*/";
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((getAlias() == null) ? 0 : getAlias().hashCode());
	result = prime * result + ((entityType == null) ? 0 : entityType.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (!(obj instanceof EntQuerySourceFromEntityType)) {
	    return false;
	}
	final EntQuerySourceFromEntityType other = (EntQuerySourceFromEntityType) obj;
	if (getAlias() == null) {
	    if (other.getAlias() != null) {
		return false;
	    }
	} else if (!getAlias().equals(other.getAlias())) {
	    return false;
	}
	if (entityType == null) {
	    if (other.entityType != null) {
		return false;
	    }
	} else if (!entityType.equals(other.entityType)) {
	    return false;
	}
	return true;
    }

    @Override
    public String sql() {
	return sourceType().getSimpleName().toUpperCase() + "_ AS " + sqlAlias + "/*" + alias + "*/";
    }

    @Override
    Pair<String, Class> lookForProp(final String dotNotatedPropName) {
	return lookForPropOnPropTypeLevel(EntityUtils.splitPropByFirstDot(dotNotatedPropName).getKey(), sourceType(), dotNotatedPropName);
    }
}