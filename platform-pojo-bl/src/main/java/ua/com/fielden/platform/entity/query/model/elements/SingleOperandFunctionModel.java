package ua.com.fielden.platform.entity.query.model.elements;

import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.query.model.structure.ISingleOperand;

abstract class SingleOperandFunctionModel implements ISingleOperand {

    private final ISingleOperand operand;

    public SingleOperandFunctionModel(final ISingleOperand operand) {
	super();
	this.operand = operand;
    }

    @Override
    public Set<String> getPropNames() {
	final Set<String> result = new HashSet<String>();
	result.addAll(operand.getPropNames());
	return result;
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
	if (!(obj instanceof SingleOperandFunctionModel)) {
	    return false;
	}
	final SingleOperandFunctionModel other = (SingleOperandFunctionModel) obj;
	if (operand == null) {
	    if (other.operand != null) {
		return false;
	    }
	} else if (!operand.equals(other.operand)) {
	    return false;
	}
	return true;
    }
}