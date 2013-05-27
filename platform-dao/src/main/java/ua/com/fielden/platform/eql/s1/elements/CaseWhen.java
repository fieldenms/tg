package ua.com.fielden.platform.eql.s1.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.eql.s2.elements.ICondition2;
import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;
import ua.com.fielden.platform.utils.Pair;


public class CaseWhen extends AbstractFunction<ua.com.fielden.platform.eql.s2.elements.CaseWhen> {

    private List<Pair<ICondition<? extends ICondition2>, ISingleOperand<? extends ISingleOperand2>>> whenThenPairs = new ArrayList<>();
    private final ISingleOperand<? extends ISingleOperand2> elseOperand;

    public CaseWhen(final List<Pair<ICondition<? extends ICondition2>, ISingleOperand<? extends ISingleOperand2>>> whenThenPairs, final ISingleOperand<? extends ISingleOperand2> elseOperand) {
	super();
	this.whenThenPairs.addAll(whenThenPairs);
	this.elseOperand = elseOperand;
    }

    @Override
    public ua.com.fielden.platform.eql.s2.elements.CaseWhen transform() {
	final List<Pair<ICondition2, ISingleOperand2>> transformedWhenThenPairs = new ArrayList<>();
	for (final Pair<ICondition<? extends ICondition2>, ISingleOperand<? extends ISingleOperand2>> pair : whenThenPairs) {
	    transformedWhenThenPairs.add(new Pair<ICondition2, ISingleOperand2>(pair.getKey().transform(), pair.getValue().transform()));
	}
	return new ua.com.fielden.platform.eql.s2.elements.CaseWhen(transformedWhenThenPairs, elseOperand.transform());
    }

    @Override
    public List<EntQuery> getLocalSubQueries() {
	final List<EntQuery> result = new ArrayList<EntQuery>();
	for (final Pair<ICondition<? extends ICondition2>, ISingleOperand<? extends ISingleOperand2>> whenThen : whenThenPairs) {
	    result.addAll(whenThen.getKey().getLocalSubQueries());
	    result.addAll(whenThen.getValue().getLocalSubQueries());
	}
	if (elseOperand != null) {
	    result.addAll(elseOperand.getLocalSubQueries());
	}
	return result;
    }

    @Override
    public List<EntProp> getLocalProps() {
	final List<EntProp> result = new ArrayList<EntProp>();
	for (final Pair<ICondition<? extends ICondition2>, ISingleOperand<? extends ISingleOperand2>> whenThen : whenThenPairs) {
	    result.addAll(whenThen.getKey().getLocalProps());
	    result.addAll(whenThen.getValue().getLocalProps());
	}
	if (elseOperand != null) {
	    result.addAll(elseOperand.getLocalProps());
	}
	return result;
    }

    @Override
    public List<EntValue> getAllValues() {
	final List<EntValue> result = new ArrayList<EntValue>();
	for (final Pair<ICondition<? extends ICondition2>, ISingleOperand<? extends ISingleOperand2>> whenThen : whenThenPairs) {
	    result.addAll(whenThen.getKey().getAllValues());
	    result.addAll(whenThen.getValue().getAllValues());
	}
	if (elseOperand != null) {
	    result.addAll(elseOperand.getAllValues());
	}
	return result;
    }

    @Override
    public boolean ignore() {
	return false;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((elseOperand == null) ? 0 : elseOperand.hashCode());
	result = prime * result + ((whenThenPairs == null) ? 0 : whenThenPairs.hashCode());
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
	if (!(obj instanceof CaseWhen)) {
	    return false;
	}
	final CaseWhen other = (CaseWhen) obj;
	if (elseOperand == null) {
	    if (other.elseOperand != null) {
		return false;
	    }
	} else if (!elseOperand.equals(other.elseOperand)) {
	    return false;
	}
	if (whenThenPairs == null) {
	    if (other.whenThenPairs != null) {
		return false;
	    }
	} else if (!whenThenPairs.equals(other.whenThenPairs)) {
	    return false;
	}
	return true;
    }
}