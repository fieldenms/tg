package ua.com.fielden.platform.eql.s1.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.LogicalOperator;
import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.Conditions2;
import ua.com.fielden.platform.eql.s2.elements.ICondition2;

public class Conditions1 extends AbstractCondition1<Conditions2> {
    private final List<List<ICondition1<? extends ICondition2>>> allConditions = new ArrayList<>();

    public Conditions1(final ICondition1<? extends ICondition2> firstCondition, final List<CompoundCondition1> otherConditions) {
	this.allConditions.addAll(formConditionIntoLogicalGroups(firstCondition, otherConditions));
    }

    public Conditions1(final ICondition1<? extends ICondition2> firstCondition) {
	this(firstCondition, Collections.<CompoundCondition1>emptyList());
    }

    public Conditions1() {
    }

    private List<List<ICondition1<? extends ICondition2>>> formConditionIntoLogicalGroups(final ICondition1<? extends ICondition2> firstCondition, final List<CompoundCondition1> otherConditions) {
	final List<List<ICondition1<? extends ICondition2>>> result = new ArrayList<>();
	List<ICondition1<? extends ICondition2>> currGroup = new ArrayList<ICondition1<? extends ICondition2>>();

	if (firstCondition != null && !firstCondition.ignore()) {
	    currGroup.add(firstCondition);
	}

	for (final CompoundCondition1 compoundCondition : otherConditions) {
	    if (compoundCondition.getLogicalOperator() == LogicalOperator.AND) {
		if (!compoundCondition.getCondition().ignore()) {
		    currGroup.add(compoundCondition.getCondition());
		}
	    } else {
		if (currGroup.size() > 0) {
		    result.add(currGroup);
		}

		currGroup = new ArrayList<ICondition1<? extends ICondition2>>();
		if (!compoundCondition.getCondition().ignore()) {
		    currGroup.add(compoundCondition.getCondition());
		}
	    }
	}

	if (currGroup.size() > 0) {
	    result.add(currGroup);
	}

	return result;
    }

    @Override
    public Conditions2 transform(final TransformatorToS2 resolver) {

	final List<List<ICondition2>> transformed = new ArrayList<>();
	for (final List<ICondition1<? extends ICondition2>> conditionGroup : allConditions) {
	    final List<ICondition2> transformedGroup = new ArrayList<>();
	    for (final ICondition1<? extends ICondition2> condition : conditionGroup) {
		transformedGroup.add(condition.transform(resolver));
	    }
	    transformed.add(transformedGroup);
	}
	return new Conditions2(transformed);
    }

    @Override
    public boolean ignore() {
	return allConditions.size() == 0;
    }

    @Override
    protected List<IElement1> getCollection() {
	final List<IElement1> result = new ArrayList<IElement1>();

	for (final List<ICondition1<? extends ICondition2>> compoundCondition : allConditions) {
	    for (final ICondition1<? extends ICondition2> iCondition1 : compoundCondition) {
		if (!iCondition1.ignore()) {
		    result.add(iCondition1);
		}
	    }
	}
	return result;
    }

    //    @Override
    // TODO EQL
    //    public String toString() {
    //        final StringBuffer sb = new StringBuffer();
    //        sb.append(firstCondition);
    //        for (final CompoundCondition1 compound : otherConditions) {
    //            sb.append(" " + compound);
    //        }
    //        return sb.toString();
    //    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((allConditions == null) ? 0 : allConditions.hashCode());
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
	if (allConditions == null) {
	    if (other.allConditions != null) {
		return false;
	    }
	} else if (!allConditions.equals(other.allConditions)) {
	    return false;
	}
	return true;
    }
}