package ua.com.fielden.platform.utils;

import java.io.Serializable;
import java.util.Map;

/**
 * Represents a pair of instances logically tied together.
 * 
 * @author TG Team
 * 
 */
public class Pair<K, V> implements Map.Entry<K, V>, Serializable {
    private static final long serialVersionUID = 5789592731906172911L;

    private final K key;
    private V value;

    /**
     * Used mainly for serialisation.
     */
    protected Pair() {
        key = null;
    }

    public Pair(final K key, final V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(final V value) {
        this.value = value;
        return value;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Pair)) {
            return false;
        }
        final Pair<?, ?> cmpTo = (Pair<?, ?>) obj;
        return (getKey() == null ? cmpTo.getKey() == null : getKey().equals(cmpTo.getKey()))//
                && (getValue() == null ? getValue() == null : getValue().equals(cmpTo.getValue()));
    }

    @Override
    public int hashCode() {
        return (getKey() == null ? 0 : getKey().hashCode() * 13) + (getValue() == null ? 0 : getValue().hashCode() * 29);
    }

    @Override
    public String toString() {
        return getKey() + ": " + getValue();
    }
}
