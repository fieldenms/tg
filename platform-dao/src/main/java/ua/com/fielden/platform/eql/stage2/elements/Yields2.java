package ua.com.fielden.platform.eql.stage2.elements;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class Yields2 {
    private final SortedMap<String, Yield2> yieldsMap = new TreeMap<String, Yield2>();

    public Yields2(List<Yield2> yields) {
        for (Yield2 yield : yields) {
            yieldsMap.put(yield.getAlias(), yield);
        }
    }
    
    public Collection<Yield2> getYields() {
        return Collections.unmodifiableCollection(yieldsMap.values());
    }

    @Override
    public String toString() {
        return yieldsMap.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((yieldsMap == null) ? 0 : yieldsMap.hashCode());
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
        if (!(obj instanceof Yields2)) {
            return false;
        }
        final Yields2 other = (Yields2) obj;
        if (yieldsMap == null) {
            if (other.yieldsMap != null) {
                return false;
            }
        } else if (!yieldsMap.equals(other.yieldsMap)) {
            return false;
        }
        return true;
    }
}