package ua.com.fielden.platform.entity.query.model.elements;

import java.util.HashSet;
import java.util.List;
import java.util.Set;



public class ConditionsModel implements IPropertyCollector {
    private final ICondition firstCondition;
    private final List<CompoundConditionModel> otherConditions;

    public ConditionsModel(final ICondition firstCondition, final List<CompoundConditionModel> otherConditions) {
	this.firstCondition = firstCondition;
	this.otherConditions = otherConditions;
    }

    @Override
    public Set<String> getPropNames() {
	final Set<String> result = new HashSet<String>();
	result.addAll(getFirstCondition().getPropNames());

	for (final CompoundConditionModel compCondModel : getOtherConditions()) {
	    result.addAll(compCondModel.getCondition().getPropNames());
	}

	return result;
    }

    public ICondition getFirstCondition() {
        return firstCondition;
    }

    public List<CompoundConditionModel> getOtherConditions() {
        return otherConditions;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((firstCondition == null) ? 0 : firstCondition.hashCode());
	result = prime * result + ((otherConditions == null) ? 0 : otherConditions.hashCode());
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
	if (!(obj instanceof ConditionsModel)) {
	    return false;
	}
	final ConditionsModel other = (ConditionsModel) obj;
	if (firstCondition == null) {
	    if (other.firstCondition != null) {
		return false;
	    }
	} else if (!firstCondition.equals(other.firstCondition)) {
	    return false;
	}
	if (otherConditions == null) {
	    if (other.otherConditions != null) {
		return false;
	    }
	} else if (!otherConditions.equals(other.otherConditions)) {
	    return false;
	}
	return true;
    }
}
