package ua.com.fielden.platform.entity.query.model.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.query.model.structure.ICondition;
import ua.com.fielden.platform.entity.query.model.structure.ISetOperand;
import ua.com.fielden.platform.entity.query.model.structure.ISingleOperand;


public class SetTestModel implements ICondition {
    private final ISingleOperand leftOperand;
    private final ISetOperand rightOperand;
    private final boolean negated;

    public SetTestModel(final ISingleOperand leftOperand, final boolean negated, final ISetOperand rightOperand) {
	this.leftOperand = leftOperand;
	this.rightOperand = rightOperand;
	this.negated = negated;
    }

    @Override
    public List<String> getPropNames() {
	final List<String> result = new ArrayList<String>();
	result.addAll(leftOperand.getPropNames());
	result.addAll(rightOperand.getPropNames());
	return result;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((leftOperand == null) ? 0 : leftOperand.hashCode());
	result = prime * result + (negated ? 1231 : 1237);
	result = prime * result + ((rightOperand == null) ? 0 : rightOperand.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (!(obj instanceof SetTestModel))
	    return false;
	final SetTestModel other = (SetTestModel) obj;
	if (leftOperand == null) {
	    if (other.leftOperand != null)
		return false;
	} else if (!leftOperand.equals(other.leftOperand))
	    return false;
	if (negated != other.negated)
	    return false;
	if (rightOperand == null) {
	    if (other.rightOperand != null)
		return false;
	} else if (!rightOperand.equals(other.rightOperand))
	    return false;
	return true;
    }
}
