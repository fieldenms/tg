package ua.com.fielden.platform.entity.query.model.elements;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;

public class EntQuerySourceAsEntity extends AbstractEntQuerySource {
    private final Class<? extends AbstractEntity> entityType;
    private final boolean generated;

    public EntQuerySourceAsEntity(final Class<? extends AbstractEntity> entityType, final String alias) {
    	this(entityType, alias, false);
    }

    public EntQuerySourceAsEntity(final Class<? extends AbstractEntity> entityType, final String alias, final boolean generated) {
	super(alias);
	this.entityType = entityType;
	this.generated = generated;
    }

    @Override
    public boolean generated() {
	return generated;
    }

    @Override
    public Class getType() {
	return entityType;
    }

    @Override
    public String toString() {
        return entityType.getSimpleName() + " aliased as [" + getAlias() + "]";
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

    @Override
    Pair<Boolean, Class> lookForPropInEntAggregatesType(final Class parentType, final String dotNotatedPropName) {
	throw new RuntimeException("Should not be invoked from here!");
    }
}