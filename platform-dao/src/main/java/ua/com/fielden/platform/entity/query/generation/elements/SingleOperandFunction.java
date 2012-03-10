package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.List;


abstract class SingleOperandFunction implements ISingleOperand {

    private final ISingleOperand operand;

    public SingleOperandFunction(final ISingleOperand operand) {
	super();
	this.operand = operand;
    }

    @Override
    public List<EntQuery> getLocalSubQueries() {
	return operand.getLocalSubQueries();
    }

    @Override
    public List<EntProp> getLocalProps() {
	return operand.getLocalProps();
    }

    @Override
    public List<EntValue> getAllValues() {
	return operand.getAllValues();
    }

    @Override
    public Class type() {
	return null;
    }

    @Override
    public Object hibType() {
	return null;
    }

    @Override
    public boolean isNullable() {
	return true;
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
	if (!(obj instanceof SingleOperandFunction)) {
	    return false;
	}
	final SingleOperandFunction other = (SingleOperandFunction) obj;
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

    public ISingleOperand getOperand() {
        return operand;
    }
}