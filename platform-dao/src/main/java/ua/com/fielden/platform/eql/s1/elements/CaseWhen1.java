package ua.com.fielden.platform.eql.s1.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.CaseWhen2;
import ua.com.fielden.platform.eql.s2.elements.ICondition2;
import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;
import ua.com.fielden.platform.utils.Pair;


public class CaseWhen1 extends AbstractFunction1<CaseWhen2> {

    private List<Pair<ICondition1<? extends ICondition2>, ISingleOperand1<? extends ISingleOperand2>>> whenThenPairs = new ArrayList<>();
    private final ISingleOperand1<? extends ISingleOperand2> elseOperand;

    public CaseWhen1(final List<Pair<ICondition1<? extends ICondition2>, ISingleOperand1<? extends ISingleOperand2>>> whenThenPairs, final ISingleOperand1<? extends ISingleOperand2> elseOperand) {
	super();
	this.whenThenPairs.addAll(whenThenPairs);
	this.elseOperand = elseOperand;
    }

    @Override
    public CaseWhen2 transform(final TransformatorToS2 resolver) {
	final List<Pair<ICondition2, ISingleOperand2>> transformedWhenThenPairs = new ArrayList<>();
	for (final Pair<ICondition1<? extends ICondition2>, ISingleOperand1<? extends ISingleOperand2>> pair : whenThenPairs) {
	    transformedWhenThenPairs.add(new Pair<ICondition2, ISingleOperand2>(pair.getKey().transform(resolver), pair.getValue().transform(resolver)));
	}
	return new CaseWhen2(transformedWhenThenPairs, elseOperand.transform(resolver));
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
	if (!(obj instanceof CaseWhen1)) {
	    return false;
	}
	final CaseWhen1 other = (CaseWhen1) obj;
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