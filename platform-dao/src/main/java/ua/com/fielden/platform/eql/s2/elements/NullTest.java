package ua.com.fielden.platform.eql.s2.elements;

import java.util.ArrayList;
import java.util.List;



public class NullTest extends AbstractCondition {
    private final ISingleOperand2 operand;
    private final boolean negated;

    public NullTest(final ISingleOperand2 operand, final boolean negated) {
	this.operand = operand;
	this.negated = negated;
    }

    @Override
    public boolean ignore() {
	return operand.ignore();
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
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (!(obj instanceof NullTest)) {
	    return false;
	}
	final NullTest other = (NullTest) obj;
	if (negated != other.negated) {
	    return false;
	}
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
    protected List<IElement2> getCollection() {
	return new ArrayList<IElement2>(){{add(operand);}};
    }
}