package ua.com.fielden.platform.entity.query.model.elements;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.structure.IEntQuerySource;

public class EntQuerySourceAsEntity implements IEntQuerySource {
    private final Class<? extends AbstractEntity> entityType;
    private final String alias;

    public EntQuerySourceAsEntity(final Class<? extends AbstractEntity> entityType, final String alias) {
	super();
	this.entityType = entityType;
	this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((alias == null) ? 0 : alias.hashCode());
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
	if (alias == null) {
	    if (other.alias != null) {
		return false;
	    }
	} else if (!alias.equals(other.alias)) {
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
