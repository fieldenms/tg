package ua.com.fielden.platform.entity.query;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Tree of entity's properties hierarchy and correspondent property value index in raw data array. Tree structure to contain either ENTITY or COMPOSITE VALUE OBJECT
 *
 * @author TG Team
 *
 */
public class EntityTree {
    private Class resultType; // e.g. Vehicle, ISimpleMoneyType
    private SortedMap<PropColumn, Integer/*position in raw result array*/> singles = new TreeMap<PropColumn, Integer>();
    private SortedMap<String /*composite property name*/, EntityTree> composites = new TreeMap<String, EntityTree>();

    protected EntityTree(final Class resultType) {
	this.resultType = resultType;
    }

    @Override
    public String toString() {
	return "\n\tResult type: " + resultType.getName() + "\n\tsingles: " + singles + "\n\tcomposites:" + composites;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((composites == null) ? 0 : composites.hashCode());
	result = prime * result + ((resultType == null) ? 0 : resultType.hashCode());
	result = prime * result + ((singles == null) ? 0 : singles.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (!(obj instanceof EntityTree))
	    return false;
	final EntityTree other = (EntityTree) obj;
	if (composites == null) {
	    if (other.composites != null)
		return false;
	} else if (!composites.equals(other.composites))
	    return false;
	if (resultType == null) {
	    if (other.resultType != null)
		return false;
	} else if (!resultType.equals(other.resultType))
	    return false;
	if (singles == null) {
	    if (other.singles != null)
		return false;
	} else if (!singles.equals(other.singles))
	    return false;
	return true;
    }

    public Class getResultType() {
        return resultType;
    }

    public SortedMap<PropColumn, Integer> getSingles() {
        return singles;
    }

    public SortedMap<String, EntityTree> getComposites() {
        return composites;
    }
}