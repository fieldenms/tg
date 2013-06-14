package ua.com.fielden.platform.eql.s2.elements;

import java.util.List;



abstract class SingleOperandFunction2 extends AbstractFunction2 {

    private final ISingleOperand2 operand;

    public SingleOperandFunction2(final ISingleOperand2 operand) {
	this.operand = operand;
    }

    @Override
    public List<EntValue2> getAllValues() {
	return operand.getAllValues();
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
	if (!(obj instanceof SingleOperandFunction2)) {
	    return false;
	}
	final SingleOperandFunction2 other = (SingleOperandFunction2) obj;
	if (operand == null) {
	    if (other.operand != null) {
		return false;
	    }
	} else if (!operand.equals(other.operand)) {
	    return false;
	}
	return true;
    }

    public ISingleOperand2 getOperand() {
        return operand;
    }
}