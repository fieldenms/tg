package ua.com.fielden.platform.eql.s1.elements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.Yield2;
import ua.com.fielden.platform.eql.s2.elements.Yields2;


public class Yields1 implements IElement1<Yields2> {
    private final SortedMap<String, Yield1> yields = new TreeMap<String, Yield1>();

    public Yields1() {
    }

    @Override
    public Yields2 transform(final TransformatorToS2 resolver) {
	final List<Yield2> transformed = new ArrayList<>();
	final Yields2 result = new Yields2();
	for (final Yield1 yield : yields.values()) {
	    result.addYield(new Yield2(yield.getOperand().transform(resolver), yield.getAlias(), yield.isRequiredHint()));
	}
	return result;
    }

    public void addYield(final Yield1 yield) {
	if (yields.containsKey(yield.getAlias())) {
	    throw new IllegalStateException("Query contains duplicate yields for alias [" + yield.getAlias() + "]");
	}
	yields.put(yield.getAlias(), yield);
    }

    public void removeYield(final Yield1 yield) {
	if (!yields.containsKey(yield.getAlias())) {
	    throw new IllegalStateException("Query does not contain the following yield [" + yield + "]");
	}
	yields.remove(yield.getAlias());
    }

    public void removeYields(final Set<Yield1> toBeRemoved) {
	for (final Yield1 yield : toBeRemoved) {
	    removeYield(yield);
	}
    }

    public Yield1 getFirstYield() {
	return !yields.isEmpty() ? yields.values().iterator().next() : null;
    }

    public int size() {
	return yields.size();
    }

    public Collection<Yield1> getYields() {
	return Collections.unmodifiableCollection(yields.values());
    }

    public void clear() {
	yields.clear();
    }

    public Yield1 getYieldByAlias(final String alias) {
	return yields.get(alias);
    }

    public Yield1 findMostMatchingYield(final String orderByYieldName) {
	String bestMatch = null;
	for (final String yieldName : yields.keySet()) {
	    if (orderByYieldName.startsWith(yieldName) && (bestMatch == null || (bestMatch != null && bestMatch.length() < yieldName.length()))) {
		bestMatch = yieldName;
	    }
 	}
	return bestMatch != null ? yields.get(bestMatch) : null;
    }

    @Override
    public List<EntQuery1> getLocalSubQueries() {
	final List<EntQuery1> result = new ArrayList<EntQuery1>();
	for (final Yield1 yield : yields.values()) {
//	    if (!yield.isCompositePropertyHeader()) {
		result.addAll(yield.getOperand().getLocalSubQueries());
//	    }
	}
	return result;
    }

    @Override
    public List<EntProp1> getLocalProps() {
	final List<EntProp1> result = new ArrayList<EntProp1>();
	for (final Yield1 yield : yields.values()) {
//	    if (!yield.isCompositePropertyHeader()) {
		result.addAll(yield.getOperand().getLocalProps());
//	    }
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
	if (!(obj instanceof Yields1)) {
	    return false;
	}
	final Yields1 other = (Yields1) obj;
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