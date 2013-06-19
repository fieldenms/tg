package ua.com.fielden.platform.eql.s2.elements;

import java.util.List;


public class Concat2 extends AbstractFunction2 {

    private final List<ISingleOperand2> operands;

    public Concat2(final List<ISingleOperand2> operands) {
	this.operands = operands;
    }

    @Override
    public boolean ignore() {
	return false;
    }

    public List<ISingleOperand2> getOperands() {
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
	if (!(obj instanceof Concat2)) {
	    return false;
	}
	final Concat2 other = (Concat2) obj;
	if (operands == null) {
	    if (other.operands != null) {
		return false;
	    }
	} else if (!operands.equals(other.operands)) {
	    return false;
	}
	return true;
    }

    @Override
    public Class type() {
	return String.class;
    }
}