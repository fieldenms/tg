package ua.com.fielden.platform.entity.query;

import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.entity.query.generation.elements.ResultQueryYieldDetails;

/**
 * Tree of entity's properties hierarchy and correspondent property value index in raw data array. Tree structure to contain COMPOSITE VALUE OBJECT
 * 
 * @author TG Team
 * 
 */
public class ValueTree {
    private final ICompositeUserTypeInstantiate hibType; //e.g. ISimpleMoneyType
    private SortedMap<ResultQueryYieldDetails, Integer/*position in raw result array*/> singles = new TreeMap<ResultQueryYieldDetails, Integer>();

    protected ValueTree(final ICompositeUserTypeInstantiate hibType) {
        this.hibType = hibType;
    }

    @Override
    public String toString() {
        return "\n\tResult type: " + hibType.getClass().getSimpleName() + "\n\tsingles: " + singles;
    }

    public SortedMap<ResultQueryYieldDetails, Integer> getSingles() {
        return singles;
    }

    public ICompositeUserTypeInstantiate getHibType() {
        return hibType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((hibType == null) ? 0 : hibType.hashCode());
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
        if (!(obj instanceof ValueTree)) {
            return false;
        }
        final ValueTree other = (ValueTree) obj;
        if (hibType == null) {
            if (other.hibType != null) {
                return false;
            }
        } else if (!hibType.equals(other.hibType)) {
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