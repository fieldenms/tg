package ua.com.fielden.platform.eql.s2.elements;


public class SetTest2 extends AbstractCondition2 {
    private final ISingleOperand2 leftOperand;
    private final ISetOperand2 rightOperand;
    private final boolean negated;

    public SetTest2(final ISingleOperand2 leftOperand, final boolean negated, final ISetOperand2 rightOperand) {
	this.leftOperand = leftOperand;
	this.rightOperand = rightOperand;
	this.negated = negated;
    }

    @Override
    public boolean ignore() {
	return leftOperand.ignore();
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
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (!(obj instanceof SetTest2)) {
	    return false;
	}
	final SetTest2 other = (SetTest2) obj;
	if (leftOperand == null) {
	    if (other.leftOperand != null) {
		return false;
	    }
	} else if (!leftOperand.equals(other.leftOperand)) {
	    return false;
	}
	if (negated != other.negated) {
	    return false;
	}
	if (rightOperand == null) {
	    if (other.rightOperand != null) {
		return false;
	    }
	} else if (!rightOperand.equals(other.rightOperand)) {
	    return false;
	}
	return true;
    }
}