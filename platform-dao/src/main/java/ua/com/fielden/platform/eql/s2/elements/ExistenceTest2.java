package ua.com.fielden.platform.eql.s2.elements;

import java.util.ArrayList;
import java.util.List;


public class ExistenceTest2 extends AbstractCondition2 {
    private final boolean negated;
    private final EntQuery2 subQuery;

    public ExistenceTest2(final boolean negated, final EntQuery2 subQuery) {
	this.negated = negated;
	this.subQuery = subQuery;
    }

    @Override
    public boolean ignore() {
	return false;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + (negated ? 1231 : 1237);
	result = prime * result + ((subQuery == null) ? 0 : subQuery.hashCode());
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
	if (!(obj instanceof ExistenceTest2)) {
	    return false;
	}
	final ExistenceTest2 other = (ExistenceTest2) obj;
	if (negated != other.negated) {
	    return false;
	}
	if (subQuery == null) {
	    if (other.subQuery != null) {
		return false;
	    }
	} else if (!subQuery.equals(other.subQuery)) {
	    return false;
	}
	return true;
    }

    @Override
    protected List<IElement2> getCollection() {
	return new ArrayList<IElement2>(){{add(subQuery);}};
    }
}