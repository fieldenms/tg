package ua.com.fielden.platform.eql.s2.elements;

import java.util.List;

public class GroupedConditions2 extends Conditions2 {
    private final boolean negated;

    public GroupedConditions2(final boolean negated, final List<List<ICondition2>> allConditions) {
	super(allConditions);
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
	if (!(obj instanceof GroupedConditions2)) {
	    return false;
	}
	final GroupedConditions2 other = (GroupedConditions2) obj;
	if (negated != other.negated) {
	    return false;
	}
	return true;
    }
}