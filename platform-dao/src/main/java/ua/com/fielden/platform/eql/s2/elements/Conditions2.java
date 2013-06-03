package ua.com.fielden.platform.eql.s2.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.LogicalOperator;



public class Conditions2 extends AbstractCondition2 {
    private final ICondition2 firstCondition;
    private final List<CompoundCondition2> otherConditions = new ArrayList<CompoundCondition2>();

    public Conditions2(final ICondition2 firstCondition, final List<CompoundCondition2> otherConditions) {
	this.firstCondition = firstCondition;
	this.otherConditions.addAll(otherConditions);
    }

    public Conditions2(final ICondition2 firstCondition) {
	this.firstCondition = firstCondition;
    }

    private List<List<ICondition2>> formConditionIntoLogicalGroups() {
	final List<List<ICondition2>> result = new ArrayList<List<ICondition2>>();
	List<ICondition2> currGroup = new ArrayList<ICondition2>();
	currGroup.add(firstCondition);

	for (final CompoundCondition2 compoundCondition : otherConditions) {
	    if (compoundCondition.getLogicalOperator() == LogicalOperator.AND) {
		currGroup.add(compoundCondition.getCondition());
	    } else {
		result.add(currGroup);
		currGroup = new ArrayList<ICondition2>();
		currGroup.add(compoundCondition.getCondition());
	    }
	}

	result.add(currGroup);

	return result;
    }

    @Override
    public boolean ignore() {
	if (firstCondition != null && !firstCondition.ignore()) {
	    return false;
	}

	for (final CompoundCondition2 compoundCondition : otherConditions) {
	    if (!compoundCondition.getCondition().ignore()) {
		return false;
	    }
	}

	return true;
    }

    @Override
    protected List<IElement2> getCollection() {
	final List<IElement2> result = new ArrayList<IElement2>();
	if (firstCondition != null && !firstCondition.ignore()) {
	    result.add(firstCondition);
	}

	for (final CompoundCondition2 compoundCondition : otherConditions) {
	    if (!compoundCondition.getCondition().ignore()) {
		result.add(compoundCondition.getCondition());
	    }
	}
	return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(firstCondition);
        for (final CompoundCondition2 compound : otherConditions) {
            sb.append(" " + compound);
        }
        return sb.toString();
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
	if (!(obj instanceof Conditions2)) {
	    return false;
	}
	final Conditions2 other = (Conditions2) obj;
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