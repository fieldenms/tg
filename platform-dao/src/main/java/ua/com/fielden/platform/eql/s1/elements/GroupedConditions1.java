package ua.com.fielden.platform.eql.s1.elements;

import java.util.List;

import ua.com.fielden.platform.eql.s2.elements.ICondition2;

public class GroupedConditions1 extends Conditions1 {
    private final boolean negated;

    public GroupedConditions1(final boolean negated, final ICondition1<? extends ICondition2> firstCondition, final List<CompoundCondition1> otherConditions) {
	super(firstCondition, otherConditions);
	this.negated = negated;
    }

    public GroupedConditions1(final boolean negated, final ICondition1<? extends ICondition2> firstCondition) {
	super(firstCondition);
	this.negated = negated;
    }

    @Override
    public String toString() {
        return (negated ? "NOT (" :"(") + super.toString() +")";
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = super.hashCode();
	result = prime * result + (negated ? 1231 : 1237);
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (!super.equals(obj)) {
	    return false;
	}
	if (!(obj instanceof GroupedConditions1)) {
	    return false;
	}
	final GroupedConditions1 other = (GroupedConditions1) obj;
	if (negated != other.negated) {
	    return false;
	}
	return true;
    }
}