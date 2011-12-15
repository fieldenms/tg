package ua.com.fielden.platform.entity.query.model.elements;

import java.util.Arrays;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.reflection.Finder;

public class EntQuerySourceAsModel implements IEntQuerySource {
    private final List<EntQuery> models;
    private final String alias;

    public EntQuerySourceAsModel(final String alias, final EntQuery... models) {
	super();
	this.alias = alias;
	this.models = Arrays.asList(models);
    }

    public String getAlias() {
	return alias;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((alias == null) ? 0 : alias.hashCode());
	result = prime * result + ((models == null) ? 0 : models.hashCode());
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
	if (!(obj instanceof EntQuerySourceAsModel)) {
	    return false;
	}
	final EntQuerySourceAsModel other = (EntQuerySourceAsModel) obj;
	if (alias == null) {
	    if (other.alias != null) {
		return false;
	    }
	} else if (!alias.equals(other.alias)) {
	    return false;
	}
	if (models == null) {
	    if (other.models != null) {
		return false;
	    }
	} else if (!models.equals(other.models)) {
	    return false;
	}
	return true;
    }

    @Override
    public boolean hasProperty(final String dotNotatedPropName) {
	final Class sourceResultType = getType();

	if (AbstractEntity.class.isAssignableFrom(sourceResultType) && !EntityAggregates.class.isAssignableFrom(sourceResultType)) {
	    if (dotNotatedPropName.equalsIgnoreCase(alias)) {
		return true; // id property is meant here
	    }

	    try {
		Finder.findFieldByName(sourceResultType, (alias == null ? dotNotatedPropName : (!dotNotatedPropName.startsWith(alias + ".") ? dotNotatedPropName
			: dotNotatedPropName.substring(alias.length() + 1))));
		return true;
	    } catch (final Exception e) {
		return false;
	    }

	} else {
	    return false; // TODO need to cover case of EntityAggregates and Primitives
	}
    }

    @Override
    public Class getType() {
	return models.get(0).getResultType();
    }
}
