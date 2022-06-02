package ua.com.fielden.platform.eql.retrieval;

import static java.util.Collections.unmodifiableMap;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;

/**
 * Tree of entity's properties hierarchy and correspondent property value index in raw data array. Tree structure to contain COMPOSITE VALUE OBJECT
 * 
 * @author TG Team
 * 
 */
public class ValueTree {
    public final ICompositeUserTypeInstantiate hibType; //e.g. ISimpleMoneyType
    private Map<Integer/*position in raw result array*/, YieldDetails> singles = new HashMap<>();

    protected ValueTree(final ICompositeUserTypeInstantiate hibType, final Map<Integer, YieldDetails> singles) {
        this.hibType = hibType;
        this.singles.putAll(singles);
    }

    public Map<Integer, YieldDetails> getSingles() {
        return unmodifiableMap(singles);
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