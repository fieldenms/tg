package ua.com.fielden.platform.entity.query;

import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.generation.elements.ResultQueryYieldDetails;

/**
 * Tree of entity's properties hierarchy and correspondent property value index in raw data array. Tree structure to contain either ENTITY.
 *
 * @author TG Team
 *
 */
public class EntityTree<E extends AbstractEntity<?>> {
    private Class<E> resultType; // e.g. Vehicle
    private SortedMap<ResultQueryYieldDetails, Integer/*position in raw result array*/> singles = new TreeMap<ResultQueryYieldDetails, Integer>();
    private SortedMap<String /*composite property name*/, EntityTree<? extends AbstractEntity<?>>> composites = new TreeMap<String, EntityTree<? extends AbstractEntity<?>>>();
    private SortedMap<String /*composite value property name*/, ValueTree> compositeValues = new TreeMap<String, ValueTree>();

    protected EntityTree(final Class<E> resultType) {
	this.resultType = resultType;
    }

    @Override
    public String toString() {
	return "\n\tResult type: " + resultType.getName() + "\n\tsingles: " + singles + "\n\tcomposites:" + composites;
    }

    public Class<E> getResultType() {
        return resultType;
    }

    public SortedMap<ResultQueryYieldDetails, Integer> getSingles() {
        return singles;
    }

    public SortedMap<String, EntityTree<? extends AbstractEntity<?>>> getComposites() {
        return composites;
    }

    public SortedMap<String, ValueTree> getCompositeValues() {
        return compositeValues;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((compositeValues == null) ? 0 : compositeValues.hashCode());
	result = prime * result + ((composites == null) ? 0 : composites.hashCode());
	result = prime * result + ((resultType == null) ? 0 : resultType.hashCode());
	result = prime * result + ((singles == null) ? 0 : singles.hashCode());
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
	if (!(obj instanceof EntityTree)) {
	    return false;
	}
	final EntityTree other = (EntityTree) obj;
	if (compositeValues == null) {
	    if (other.compositeValues != null) {
		return false;
	    }
	} else if (!compositeValues.equals(other.compositeValues)) {
	    return false;
	}
	if (composites == null) {
	    if (other.composites != null) {
		return false;
	    }
	} else if (!composites.equals(other.composites)) {
	    return false;
	}
	if (resultType == null) {
	    if (other.resultType != null) {
		return false;
	    }
	} else if (!resultType.equals(other.resultType)) {
	    return false;
	}
	if (singles == null) {
	    if (other.singles != null) {
		return false;
	    }
	} else if (!singles.equals(other.singles)) {
	    return false;
	}
	return true;
    }
}