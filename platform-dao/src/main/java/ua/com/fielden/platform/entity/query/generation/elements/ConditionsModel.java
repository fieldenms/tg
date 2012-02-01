package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class ConditionsModel implements IPropertyCollector {
    private final ICondition firstCondition;
    private final List<CompoundConditionModel> otherConditions;

    public String sql() {
        final StringBuffer sb = new StringBuffer();
        sb.append(firstCondition.sql());
        for (final CompoundConditionModel compound : otherConditions) {
            sb.append(" " + compound.sql());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(firstCondition);
        for (final CompoundConditionModel compound : otherConditions) {
            sb.append(" " + compound);
        }
        return sb.toString();
    }

    public ConditionsModel(final ICondition firstCondition, final List<CompoundConditionModel> otherConditions) {
	this.firstCondition = firstCondition;
	this.otherConditions = otherConditions;
    }

    public ConditionsModel(final ICondition firstCondition) {
	this.firstCondition = firstCondition;
	this.otherConditions = Collections.emptyList();
    }

    @Override
    public List<EntQuery> getSubqueries() {
	final List<EntQuery> result = new ArrayList<EntQuery>();
	if (firstCondition != null) {
	    result.addAll(getFirstCondition().getSubqueries());
	}

	for (final CompoundConditionModel compCondModel : getOtherConditions()) {
	    result.addAll(compCondModel.getCondition().getSubqueries());
	}
	return result;
    }

    @Override
    public List<EntProp> getProps() {
	final List<EntProp> result = new ArrayList<EntProp>();
	if (firstCondition != null) {
		result.addAll(getFirstCondition().getProps());
	}

	for (final CompoundConditionModel compCondModel : getOtherConditions()) {
	    result.addAll(compCondModel.getCondition().getProps());
	}
	return result;
    }

    @Override
    public List<EntValue> getValues() {
	final List<EntValue> result = new ArrayList<EntValue>();
	if (firstCondition != null) {
	    result.addAll(getFirstCondition().getValues());
	}

	for (final CompoundConditionModel compCondModel : getOtherConditions()) {
	    result.addAll(compCondModel.getCondition().getValues());
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