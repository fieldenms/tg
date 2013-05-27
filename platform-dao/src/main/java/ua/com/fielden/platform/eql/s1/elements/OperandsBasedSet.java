package ua.com.fielden.platform.eql.s1.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;


public class OperandsBasedSet implements ISetOperand<ua.com.fielden.platform.eql.s2.elements.OperandsBasedSet>{
    private final List<ISingleOperand<? extends ISingleOperand2>> operands;

    public OperandsBasedSet(final List<ISingleOperand<? extends ISingleOperand2>> operands) {
	super();
	this.operands = operands;
    }

    @Override
    public ua.com.fielden.platform.eql.s2.elements.OperandsBasedSet transform() {
	final List<ISingleOperand2> transformedOperands = new ArrayList<>();
	for (final ISingleOperand<? extends ISingleOperand2> singleOperand : operands) {
	    transformedOperands.add(singleOperand.transform());
	}

	return new ua.com.fielden.platform.eql.s2.elements.OperandsBasedSet(transformedOperands);
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
    public List<EntQuery> getLocalSubQueries() {
	final List<EntQuery> result = new ArrayList<EntQuery>();
	for (final ISingleOperand operand : operands) {
	    result.addAll(operand.getLocalSubQueries());
	}
	return result;
    }

    @Override
    public List<EntValue> getAllValues() {
	final List<EntValue> result = new ArrayList<EntValue>();
	for (final ISingleOperand operand : operands) {
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
	if (!(obj instanceof OperandsBasedSet)) {
	    return false;
	}
	final OperandsBasedSet other = (OperandsBasedSet) obj;
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
    public boolean ignore() {
	// TODO Auto-generated method stub
	return false;
    }
}