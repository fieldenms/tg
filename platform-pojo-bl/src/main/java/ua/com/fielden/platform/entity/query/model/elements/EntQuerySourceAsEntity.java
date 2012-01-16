package ua.com.fielden.platform.entity.query.model.elements;

import java.lang.reflect.Field;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.Finder;

public class EntQuerySourceAsEntity extends AbstractEntQuerySource {
    private final Class<? extends AbstractEntity> entityType;

    public EntQuerySourceAsEntity(final Class<? extends AbstractEntity> entityType, final String alias) {
	super(alias);
	this.entityType = entityType;
    }

    @Override
    public Class getType() {
	return entityType;
    }

    @Override
    protected Class determinePropertyType(final String dotNotatedPropName) {
	if (dotNotatedPropName.equalsIgnoreCase(getAlias())) {
	    return Long.class; // id property is meant here
	}

	try {
	    final Field field = Finder.findFieldByName(entityType, dealiasPropName(dotNotatedPropName, getAlias()));
	    return field.getType();
	} catch (final Exception e) {
	    return null;
	}
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
	if (!(obj instanceof EntQuerySourceAsEntity)) {
	    return false;
	}
	final EntQuerySourceAsEntity other = (EntQuerySourceAsEntity) obj;
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
}