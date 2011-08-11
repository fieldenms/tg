package ua.com.fielden.platform.entity.query.model.elements;

import java.util.List;

import ua.com.fielden.platform.entity.query.model.structure.ICondition;



public class GroupedConditionsModel extends ConditionsModel implements ICondition {
    private final boolean negated;

    public GroupedConditionsModel(final boolean negated, final ICondition firstCondition, final List<CompoundConditionModel> otherConditions) {
	super(firstCondition, otherConditions);
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
	if (this == obj)
	    return true;
	if (!super.equals(obj))
	    return false;
	if (!(obj instanceof GroupedConditionsModel))
	    return false;
	final GroupedConditionsModel other = (GroupedConditionsModel) obj;
	if (negated != other.negated)
	    return false;
	return true;
    }
}
