package ua.com.fielden.platform.eql.s2.elements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;


public class Yields2 implements IElement2 {
    private final SortedMap<String, Yield2> yields = new TreeMap<String, Yield2>();

    public Yields2() {
    }

    public void addYield(final Yield2 yield) {
	if (yields.containsKey(yield.getAlias())) {
	    throw new IllegalStateException("Query contains duplicate yields for alias [" + yield.getAlias() + "]");
	}
	yields.put(yield.getAlias(), yield);
    }

    public void removeYield(final Yield2 yield) {
	if (!yields.containsKey(yield.getAlias())) {
	    throw new IllegalStateException("Query does not contain the following yield [" + yield + "]");
	}
	yields.remove(yield.getAlias());
    }

    public void removeYields(final Set<Yield2> toBeRemoved) {
	for (final Yield2 yield : toBeRemoved) {
	    removeYield(yield);
	}
    }

    public Yield2 getFirstYield() {
	return !yields.isEmpty() ? yields.values().iterator().next() : null;
    }

    public int size() {
	return yields.size();
    }

    public Collection<Yield2> getYields() {
	return Collections.unmodifiableCollection(yields.values());
    }

    public void clear() {
	yields.clear();
    }

    public Yield2 getYieldByAlias(final String alias) {
	return yields.get(alias);
    }

    public Yield2 findMostMatchingYield(final String orderByYieldName) {
	String bestMatch = null;
	for (final String yieldName : yields.keySet()) {
	    if (orderByYieldName.startsWith(yieldName) && (bestMatch == null || (bestMatch != null && bestMatch.length() < yieldName.length()))) {
		bestMatch = yieldName;
	    }
 	}
	return bestMatch != null ? yields.get(bestMatch) : null;
    }

    @Override
    public List<EntValue2> getAllValues() {
	final List<EntValue2> result = new ArrayList<EntValue2>();
	for (final Yield2 yield : yields.values()) {
	    if (!yield.isCompositePropertyHeader()) {
		result.addAll(yield.getOperand().getAllValues());
	    }
	}
	return result;
    }

    @Override
    public String toString() {
	return yields.toString();
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((yields == null) ? 0 : yields.hashCode());
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
	if (yields == null) {
	    if (other.yields != null) {
		return false;
	    }
	} else if (!yields.equals(other.yields)) {
	    return false;
	}
	return true;
    }
}