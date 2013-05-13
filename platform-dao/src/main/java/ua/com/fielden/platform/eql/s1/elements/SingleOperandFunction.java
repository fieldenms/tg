package ua.com.fielden.platform.eql.s1.elements;

import java.util.List;

import ua.com.fielden.platform.entity.query.generation.DbVersion;



abstract class SingleOperandFunction extends AbstractFunction implements ISingleOperand {

    private final ISingleOperand operand;

    public SingleOperandFunction(final DbVersion dbVersion, final ISingleOperand operand) {
	super(dbVersion);
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