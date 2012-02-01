package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.Arrays;
import java.util.List;


public class ExistenceTestModel implements ICondition {
    private final boolean negated;
    private final EntQuery subQuery;

    public ExistenceTestModel(final boolean negated, final EntQuery subQuery) {
	this.negated = negated;
	this.subQuery = subQuery;
    }

    @Override
    public String sql() {
	return (negated ? "NOT EXISTS " : "EXISTS ") + subQuery.sql();
    }

    @Override
    public List<EntProp> getProps() {
	return subQuery.getProps();
    }

    @Override
    public List<EntQuery> getSubqueries() {
	return Arrays.asList(new EntQuery[]{subQuery});
    }

    @Override
    public List<EntValue> getValues() {
	return subQuery.getValues();
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
	if (!(obj instanceof ExistenceTestModel)) {
	    return false;
	}
	final ExistenceTestModel other = (ExistenceTestModel) obj;
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
}