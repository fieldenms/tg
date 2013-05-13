package ua.com.fielden.platform.eql.s1.elements;

import java.util.List;

public class GroupedConditions extends Conditions {
    private final boolean negated;

    public GroupedConditions(final boolean negated, final ICondition firstCondition, final List<CompoundCondition> otherConditions) {
	super(firstCondition, otherConditions);
	this.negated = negated;
    }

    public GroupedConditions(final boolean negated, final ICondition firstCondition) {
	super(firstCondition);
	this.negated = negated;
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
	if (!(obj instanceof GroupedConditions)) {
	    return false;
	}
	final GroupedConditions other = (GroupedConditions) obj;
	if (negated != other.negated) {
	    return false;
	}
	return true;
    }
}