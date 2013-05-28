package ua.com.fielden.platform.eql.s1.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.LogicalOperator;
import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.ICondition2;



public class Conditions extends AbstractCondition<ua.com.fielden.platform.eql.s2.elements.Conditions> {
    private final ICondition<? extends ICondition2> firstCondition;
    private final List<CompoundCondition> otherConditions = new ArrayList<CompoundCondition>();

    public Conditions(final ICondition<? extends ICondition2> firstCondition, final List<CompoundCondition> otherConditions) {
	this.firstCondition = firstCondition;
	this.otherConditions.addAll(otherConditions);
    }

    public Conditions(final ICondition<? extends ICondition2> firstCondition) {
	this.firstCondition = firstCondition;
    }

    @Override
    public ua.com.fielden.platform.eql.s2.elements.Conditions transform(final TransformatorToS2 resolver) {
	final List<ua.com.fielden.platform.eql.s2.elements.CompoundCondition> transformed = new ArrayList<>();
	for (final CompoundCondition compoundCondition : otherConditions) {
	    transformed.add(new ua.com.fielden.platform.eql.s2.elements.CompoundCondition(compoundCondition.getLogicalOperator(), compoundCondition.getCondition().transform(resolver)));
	}
	return new ua.com.fielden.platform.eql.s2.elements.Conditions(firstCondition.transform(resolver), transformed);
    }

    private List<List<ICondition>> formConditionIntoLogicalGroups() {
	final List<List<ICondition>> result = new ArrayList<List<ICondition>>();
	List<ICondition> currGroup = new ArrayList<ICondition>();
	currGroup.add(firstCondition);

	for (final CompoundCondition compoundCondition : otherConditions) {
	    if (compoundCondition.getLogicalOperator() == LogicalOperator.AND) {
		currGroup.add(compoundCondition.getCondition());
	    } else {
		result.add(currGroup);
		currGroup = new ArrayList<ICondition>();
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

	for (final CompoundCondition compoundCondition : otherConditions) {
	    if (!compoundCondition.getCondition().ignore()) {
		return false;
	    }
	}

	return true;
    }

    @Override
    protected List<IElement> getCollection() {
	final List<IElement> result = new ArrayList<IElement>();
	if (firstCondition != null && !firstCondition.ignore()) {
	    result.add(firstCondition);
	}

	for (final CompoundCondition compoundCondition : otherConditions) {
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
        for (final CompoundCondition compound : otherConditions) {
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
	if (!(obj instanceof Conditions)) {
	    return false;
	}
	final Conditions other = (Conditions) obj;
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