package ua.com.fielden.platform.eql.s2.elements;

import java.util.ArrayList;
import java.util.List;


public class LikeTest2 extends AbstractCondition2 {
    private final ISingleOperand2 leftOperand;
    private final ISingleOperand2 rightOperand;
    private final boolean negated;
    private final boolean caseInsensitive;

    public LikeTest2(final ISingleOperand2 leftOperand, final ISingleOperand2 rightOperand, final boolean negated, final boolean caseInsensitive) {
	this.leftOperand = leftOperand;
	this.rightOperand = rightOperand;
	this.negated = negated;
	this.caseInsensitive = caseInsensitive;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + (caseInsensitive ? 1231 : 1237);
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
	if (!(obj instanceof LikeTest2)) {
	    return false;
	}
	final LikeTest2 other = (LikeTest2) obj;
	if (caseInsensitive != other.caseInsensitive) {
	    return false;
	}
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

    @Override
    protected List<IElement2> getCollection() {
	return new ArrayList<IElement2>(){{add(leftOperand); add(rightOperand);}};
    }
}