package ua.com.fielden.platform.eql.retrieval;

import static java.util.Collections.unmodifiableMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Tree of entity's properties hierarchy and correspondent property value index in raw data array. Tree structure to contain either ENTITY.
 * 
 * @author TG Team
 * 
 */
public class EntityTree<E extends AbstractEntity<?>> {
    public Class<E> resultType; // e.g. Vehicle
    private Map<Integer/*position in raw result array*/, YieldDetails> singles = new HashMap<>();
    private Map<String /*composite property name*/, EntityTree<? extends AbstractEntity<?>>> composites = new HashMap<>();
    private Map<String /*composite value property name*/, ValueTree> compositeValues = new HashMap<>();

    protected EntityTree(final Class<E> resultType, 
            final Map<Integer/*position in raw result array*/, YieldDetails> singles, 
            final Map<String /*composite property name*/, EntityTree<? extends AbstractEntity<?>>> entities,
            final Map<String /*composite value property name*/, ValueTree> compositeValues) {
        this.resultType = resultType;
        this.singles.putAll(singles);
        this.composites.putAll(entities);
        this.compositeValues.putAll(compositeValues);
    }

    public Map<Integer, YieldDetails> getSingles() {
        return unmodifiableMap(singles);
    }

    public Map<String, EntityTree<? extends AbstractEntity<?>>> getComposites() {
        return unmodifiableMap(composites);
    }

    public Map<String, ValueTree> getCompositeValues() {
        return unmodifiableMap(compositeValues);
    }
    
    public Collection<YieldDetails> getSortedScalars() {
        // TODO try without recreating sorted map and compare performance (need to return SortedMap from getScalarFromEntityTree)
        return new TreeMap<Integer, YieldDetails>(getScalarFromEntityTree()).values();
    }
    
    private Map<Integer, YieldDetails> getScalarFromEntityTree() {
        final Map<Integer, YieldDetails> result = new HashMap<>();

        result.putAll(singles);

        for (final ValueTree composite : compositeValues.values()) {
            result.putAll(composite.getSingles());
        }

        for (final Map.Entry<String, EntityTree<? extends AbstractEntity<?>>> composite : composites.entrySet()) {
            result.putAll(composite.getValue().getScalarFromEntityTree());
        }

        return result;
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