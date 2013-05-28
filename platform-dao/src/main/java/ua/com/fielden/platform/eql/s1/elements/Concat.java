package ua.com.fielden.platform.eql.s1.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;


public class Concat extends AbstractFunction<ua.com.fielden.platform.eql.s2.elements.Concat> {

    private final List<ISingleOperand<? extends ISingleOperand2>> operands;

    public Concat(final List<ISingleOperand<? extends ISingleOperand2>> operands) {
	this.operands = operands;
    }

    @Override
    public ua.com.fielden.platform.eql.s2.elements.Concat transform(final TransformatorToS2 resolver) {
	final List<ISingleOperand2> transformed = new ArrayList<>();
	for (final ISingleOperand<? extends ISingleOperand2> operand : operands) {
	    transformed.add(operand.transform(resolver));
	}
	return new ua.com.fielden.platform.eql.s2.elements.Concat(transformed);
    }

    @Override
    public List<EntQuery> getLocalSubQueries() {
	final List<EntQuery> result = new ArrayList<EntQuery>();
	for (final ISingleOperand operand : operands) {
	    result.addAll(operand.getLocalSubQueries());
	}
	return result;
    }

    @Override
    public List<EntProp> getLocalProps() {
	final List<EntProp> result = new ArrayList<EntProp>();
	for (final ISingleOperand operand : operands) {
	    result.addAll(operand.getLocalProps());
	}
	return result;
    }

    @Override
    public boolean ignore() {
	return false;
    }

    public List<ISingleOperand<? extends ISingleOperand2>> getOperands() {
        return operands;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((operands == null) ? 0 : operands.hashCode());
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
	if (!(obj instanceof Concat)) {
	    return false;
	}
	final Concat other = (Concat) obj;
	if (operands == null) {
	    if (other.operands != null) {
		return false;
	    }
	} else if (!operands.equals(other.operands)) {
	    return false;
	}
	return true;
    }
}