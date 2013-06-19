package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;


abstract class TwoOperandsFunction1<S2 extends ISingleOperand2> extends AbstractFunction1<S2> {
    private final ISingleOperand1<? extends ISingleOperand2> operand1;
    private final ISingleOperand1<? extends ISingleOperand2> operand2;

    public TwoOperandsFunction1(final ISingleOperand1<? extends ISingleOperand2> operand1, final ISingleOperand1<? extends ISingleOperand2> operand2) {
	this.operand1 = operand1;
	this.operand2 = operand2;
    }

    public ISingleOperand1<? extends ISingleOperand2> getOperand1() {
        return operand1;
    }

    public ISingleOperand1<? extends ISingleOperand2> getOperand2() {
        return operand2;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((operand1 == null) ? 0 : operand1.hashCode());
	result = prime * result + ((operand2 == null) ? 0 : operand2.hashCode());
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
	if (!(obj instanceof TwoOperandsFunction1)) {
	    return false;
	}
	final TwoOperandsFunction1 other = (TwoOperandsFunction1) obj;
	if (operand1 == null) {
	    if (other.operand1 != null) {
		return false;
	    }
	} else if (!operand1.equals(other.operand1)) {
	    return false;
	}
	if (operand2 == null) {
	    if (other.operand2 != null) {
		return false;
	    }
	} else if (!operand2.equals(other.operand2)) {
	    return false;
	}
	return true;
    }
}