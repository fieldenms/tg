package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;


public class Yields implements IPropertyCollector {
    private final SortedMap<String, Yield> yields;

    public Yields(final SortedMap<String, Yield> yields) {
	this.yields = yields;
    }

    @Override
    public List<EntValue> getAllValues() {
	final List<EntValue> result = new ArrayList<EntValue>();
	for (final Yield yield : yields.values()) {
	    result.addAll(yield.getOperand().getAllValues());
	}
	return result;
    }

    @Override
    public List<EntQuery> getLocalSubQueries() {
	final List<EntQuery> result = new ArrayList<EntQuery>();
	for (final Yield yield : yields.values()) {
	    result.addAll(yield.getOperand().getLocalSubQueries());
	}
	return result;
    }

    @Override
    public List<EntProp> getLocalProps() {
	final List<EntProp> result = new ArrayList<EntProp>();
	for (final Yield yield : yields.values()) {
	    result.addAll(yield.getOperand().getLocalProps());
	}
	return result;
    }

    public String sql() {
	final StringBuffer sb = new StringBuffer();
	for (final Iterator<Yield> iterator = yields.values().iterator(); iterator.hasNext();) {
	    final Yield yieldModel = iterator.next();
	    sb.append(yieldModel.sql());
	    if (iterator.hasNext()) {
		sb.append(", ");
	    }
	}

	return sb.toString();
    }

    @Override
    public String toString() {
	return yields.toString();
    }

    public SortedMap<String, Yield> getYields() {
        return yields;
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
}