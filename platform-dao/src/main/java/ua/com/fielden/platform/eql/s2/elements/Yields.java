package ua.com.fielden.platform.eql.s2.elements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;


public class Yields implements IElement2 {
    private final SortedMap<String, Yield> yields = new TreeMap<String, Yield>();

    public Yields() {
    }

    public void addYield(final Yield yield) {
	if (yields.containsKey(yield.getAlias())) {
	    throw new IllegalStateException("Query contains duplicate yields for alias [" + yield.getAlias() + "]");
	}
	yields.put(yield.getAlias(), yield);
    }

    public void removeYield(final Yield yield) {
	if (!yields.containsKey(yield.getAlias())) {
	    throw new IllegalStateException("Query does not contain the following yield [" + yield + "]");
	}
	yields.remove(yield.getAlias());
    }

    public void removeYields(final Set<Yield> toBeRemoved) {
	for (final Yield yield : toBeRemoved) {
	    removeYield(yield);
	}
    }

    public Yield getFirstYield() {
	return !yields.isEmpty() ? yields.values().iterator().next() : null;
    }

    public int size() {
	return yields.size();
    }

    public Collection<Yield> getYields() {
	return Collections.unmodifiableCollection(yields.values());
    }

    public void clear() {
	yields.clear();
    }

    public Yield getYieldByAlias(final String alias) {
	return yields.get(alias);
    }

    public Yield findMostMatchingYield(final String orderByYieldName) {
	String bestMatch = null;
	for (final String yieldName : yields.keySet()) {
	    if (orderByYieldName.startsWith(yieldName) && (bestMatch == null || (bestMatch != null && bestMatch.length() < yieldName.length()))) {
		bestMatch = yieldName;
	    }
 	}
	return bestMatch != null ? yields.get(bestMatch) : null;
    }

    @Override
    public List<EntValue> getAllValues() {
	final List<EntValue> result = new ArrayList<EntValue>();
	for (final Yield yield : yields.values()) {
	    if (!yield.isCompositePropertyHeader()) {
		result.addAll(yield.getOperand().getAllValues());
	    }
	}
	return result;
    }

    @Override
    public List<EntQuery> getLocalSubQueries() {
	final List<EntQuery> result = new ArrayList<EntQuery>();
	for (final Yield yield : yields.values()) {
	    if (!yield.isCompositePropertyHeader()) {
		result.addAll(yield.getOperand().getLocalSubQueries());
	    }
	}
	return result;
    }

    @Override
    public List<EntProp> getLocalProps() {
	final List<EntProp> result = new ArrayList<EntProp>();
	for (final Yield yield : yields.values()) {
	    if (!yield.isCompositePropertyHeader()) {
		result.addAll(yield.getOperand().getLocalProps());
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
	if (!(obj instanceof Yields)) {
	    return false;
	}
	final Yields other = (Yields) obj;
	if (yields == null) {
	    if (other.yields != null) {
		return false;
	    }
	} else if (!yields.equals(other.yields)) {
	    return false;
	}
	return true;
    }

    @Override
    public boolean ignore() {
	// TODO Auto-generated method stub
	return false;
    }
}