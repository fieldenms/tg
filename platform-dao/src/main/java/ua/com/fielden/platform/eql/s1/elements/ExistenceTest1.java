package ua.com.fielden.platform.eql.s1.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.ExistenceTest2;


public class ExistenceTest1 extends AbstractCondition1<ExistenceTest2> {
    private final boolean negated;
    private final EntQuery1 subQuery;

    public ExistenceTest1(final boolean negated, final EntQuery1 subQuery) {
	this.negated = negated;
	this.subQuery = subQuery;
    }

    @Override
    public ExistenceTest2 transform(final TransformatorToS2 resolver) {
	return new ExistenceTest2(negated, subQuery.transform(resolver));
    }

    @Override
    public List<EntQuery1> getLocalSubQueries() {
	return Arrays.asList(new EntQuery1[]{subQuery});
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
	if (!(obj instanceof ExistenceTest1)) {
	    return false;
	}
	final ExistenceTest1 other = (ExistenceTest1) obj;
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
    protected List<IElement1> getCollection() {
	return new ArrayList<IElement1>(){{add(subQuery);}};
    }
}