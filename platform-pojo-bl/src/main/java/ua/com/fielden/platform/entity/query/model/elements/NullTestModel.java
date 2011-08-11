package ua.com.fielden.platform.entity.query.model.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.query.model.structure.ICondition;
import ua.com.fielden.platform.entity.query.model.structure.ISingleOperand;


public class NullTestModel implements ICondition {
    private final ISingleOperand operand;
    private final boolean negated;

    public NullTestModel(final ISingleOperand operand, final boolean negated) {
	this.operand = operand;
	this.negated = negated;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + (negated ? 1231 : 1237);
	result = prime * result + ((operand == null) ? 0 : operand.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (!(obj instanceof NullTestModel))
	    return false;
	final NullTestModel other = (NullTestModel) obj;
	if (negated != other.negated)
	    return false;
	if (operand == null) {
	    if (other.operand != null)
		return false;
	} else if (!operand.equals(other.operand))
	    return false;
	return true;
    }

    @Override
    public List<String> getPropNames() {
	final List<String> result = new ArrayList<String>();
	result.addAll(operand.getPropNames());
	return result;
    }
}
