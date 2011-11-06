package ua.com.fielden.platform.entity.query.model.elements;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.Finder;

public class EntQuerySourceAsEntity implements IEntQuerySource {
    private final Class<? extends AbstractEntity> entityType;
    private final String alias; // can be also dot.notated, but should stick to property alias naming rules (e.g. no dots in beginning/end

    public EntQuerySourceAsEntity(final Class<? extends AbstractEntity> entityType, final String alias) {
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

    @Override
    public boolean hasProperty(final String dotNotatedPropName) {
	if (dotNotatedPropName.equalsIgnoreCase(alias)) {
	    return true; // id property is meant here
	}

	try {
	    Finder.findFieldByName(entityType, (alias == null ? dotNotatedPropName : (!dotNotatedPropName.startsWith(alias + ".") ? dotNotatedPropName : dotNotatedPropName.substring(alias.length() + 1))));
	    return true;
	} catch (final Exception e) {
	    return false;
	}
    }
}