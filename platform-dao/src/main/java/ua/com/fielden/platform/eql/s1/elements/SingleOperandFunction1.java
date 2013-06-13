package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;



abstract class SingleOperandFunction1<S2 extends ISingleOperand2> extends AbstractFunction1<S2> {

    private final ISingleOperand1<? extends ISingleOperand2> operand;

    public SingleOperandFunction1(final ISingleOperand1<? extends ISingleOperand2> operand) {
	this.operand = operand;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((operand == null) ? 0 : operand.hashCode());
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
	if (!(obj instanceof SingleOperandFunction1)) {
	    return false;
	}
	final SingleOperandFunction1 other = (SingleOperandFunction1) obj;
	if (operand == null) {
	    if (other.operand != null) {
		return false;
	    }
	} else if (!operand.equals(other.operand)) {
	    return false;
	}
	return true;
    }

    @Override
    public boolean ignore() {
	return false;
    }

    public ISingleOperand1<? extends ISingleOperand2> getOperand() {
        return operand;
    }
}