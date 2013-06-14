package ua.com.fielden.platform.eql.s2.elements;

import java.util.ArrayList;
import java.util.List;


public class OperandsBasedSet2 implements ISetOperand2{
    private final List<ISingleOperand2> operands;

    public OperandsBasedSet2(final List<ISingleOperand2> operands) {
	super();
	this.operands = operands;
    }

    @Override
    public List<EntValue2> getAllValues() {
	final List<EntValue2> result = new ArrayList<EntValue2>();
	for (final ISingleOperand2 operand : operands) {
	    result.addAll(operand.getAllValues());
	}
	return result;
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
	if (!(obj instanceof OperandsBasedSet2)) {
	    return false;
	}
	final OperandsBasedSet2 other = (OperandsBasedSet2) obj;
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