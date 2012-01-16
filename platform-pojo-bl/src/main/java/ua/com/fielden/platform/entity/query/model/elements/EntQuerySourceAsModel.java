package ua.com.fielden.platform.entity.query.model.elements;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.reflection.Finder;

public class EntQuerySourceAsModel extends AbstractEntQuerySource {
    private final List<EntQuery> models;

    public EntQuerySourceAsModel(final String alias, final EntQuery... models) {
	super(alias);
	this.models = Arrays.asList(models);
    }

    @Override
    public Class getType() {
	return models.get(0).getResultType();
    }

    @Override
    protected Class determinePropertyType(final String dotNotatedPropName) {
	final Class sourceResultType = getType();

	if (AbstractEntity.class.isAssignableFrom(sourceResultType) && !EntityAggregates.class.isAssignableFrom(sourceResultType)) {
	    if (dotNotatedPropName.equalsIgnoreCase(getAlias())) {
		return Long.class; // id property is meant here
	    }

	    try {
		final Field field = Finder.findFieldByName(sourceResultType, dealiasPropName(dotNotatedPropName, getAlias()));
		return field.getType();
	    } catch (final Exception e) {
		return null;
	    }

	} else if (EntityAggregates.class.isAssignableFrom(sourceResultType)) {
	    final String delalisedDotNotatedPropName = dealiasPropName(dotNotatedPropName, getAlias());
	    final String firstLevelPropName = delalisedDotNotatedPropName.contains(".") ? delalisedDotNotatedPropName.substring(0, delalisedDotNotatedPropName.indexOf("."))
		    : delalisedDotNotatedPropName;
	    final YieldModel firstLevelPropYield = models.get(0).getYield(firstLevelPropName);
	    if (firstLevelPropYield == null) {
		return null;
	    } else if (firstLevelPropYield.getOperand().type() == null) {
		if (firstLevelPropName.equalsIgnoreCase(delalisedDotNotatedPropName)) {
		    return Class.class;
		} else {
		    return null;
		}
	    } else {
		final Class firstLevelPropType = firstLevelPropYield.getOperand().type();
		if (AbstractEntity.class.isAssignableFrom(firstLevelPropType)) {
		    if (firstLevelPropName.equalsIgnoreCase(delalisedDotNotatedPropName)) {
			return firstLevelPropType;
		    } else {
			try {
			    final Field field = Finder.findFieldByName(sourceResultType, dealiasPropName(delalisedDotNotatedPropName, firstLevelPropName));
			    return field.getType();
			} catch (final Exception e) {
			    return null;
			}
		    }
		} else {
		    return firstLevelPropType;
		}
	    }
	} else {
	    return sourceResultType;
	}
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((getAlias() == null) ? 0 : getAlias().hashCode());
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
	if (getAlias() == null) {
	    if (other.getAlias() != null) {
		return false;
	    }
	} else if (!getAlias().equals(other.getAlias())) {
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
}