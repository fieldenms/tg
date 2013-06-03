package ua.com.fielden.platform.eql.s1.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.LogicalOperator;
import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.CompoundCondition2;
import ua.com.fielden.platform.eql.s2.elements.Conditions2;
import ua.com.fielden.platform.eql.s2.elements.ICondition2;



public class Conditions1 extends AbstractCondition1<Conditions2> {
    private final ICondition1<? extends ICondition2> firstCondition;
    private final List<CompoundCondition1> otherConditions = new ArrayList<CompoundCondition1>();

    public Conditions1(final ICondition1<? extends ICondition2> firstCondition, final List<CompoundCondition1> otherConditions) {
	this.firstCondition = firstCondition;
	this.otherConditions.addAll(otherConditions);
    }

    public Conditions1(final ICondition1<? extends ICondition2> firstCondition) {
	this.firstCondition = firstCondition;
    }

    @Override
    public Conditions2 transform(final TransformatorToS2 resolver) {
	final List<CompoundCondition2> transformed = new ArrayList<>();
	for (final CompoundCondition1 compoundCondition : otherConditions) {
	    transformed.add(new CompoundCondition2(compoundCondition.getLogicalOperator(), compoundCondition.getCondition().transform(resolver)));
	}
	return firstCondition != null ? new Conditions2(firstCondition.transform(resolver), transformed) : new Conditions2(null);
    }

    private List<List<ICondition1>> formConditionIntoLogicalGroups() {
	final List<List<ICondition1>> result = new ArrayList<List<ICondition1>>();
	List<ICondition1> currGroup = new ArrayList<ICondition1>();
	currGroup.add(firstCondition);

	for (final CompoundCondition1 compoundCondition : otherConditions) {
	    if (compoundCondition.getLogicalOperator() == LogicalOperator.AND) {
		currGroup.add(compoundCondition.getCondition());
	    } else {
		result.add(currGroup);
		currGroup = new ArrayList<ICondition1>();
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

	for (final CompoundCondition1 compoundCondition : otherConditions) {
	    if (!compoundCondition.getCondition().ignore()) {
		return false;
	    }
	}

	return true;
    }

    @Override
    protected List<IElement1> getCollection() {
	final List<IElement1> result = new ArrayList<IElement1>();
	if (firstCondition != null && !firstCondition.ignore()) {
	    result.add(firstCondition);
	}

	for (final CompoundCondition1 compoundCondition : otherConditions) {
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
        for (final CompoundCondition1 compound : otherConditions) {
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
	if (!(obj instanceof Conditions1)) {
	    return false;
	}
	final Conditions1 other = (Conditions1) obj;
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