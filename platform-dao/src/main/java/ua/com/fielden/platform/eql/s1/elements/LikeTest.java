package ua.com.fielden.platform.eql.s1.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;


public class LikeTest extends AbstractCondition<ua.com.fielden.platform.eql.s2.elements.LikeTest> {
    private final ISingleOperand<? extends ISingleOperand2> leftOperand;
    private final ISingleOperand<? extends ISingleOperand2> rightOperand;
    private final boolean negated;
    private final boolean caseInsensitive;

    public LikeTest(final ISingleOperand<? extends ISingleOperand2> leftOperand, final ISingleOperand<? extends ISingleOperand2> rightOperand, final boolean negated, final boolean caseInsensitive) {
	this.leftOperand = leftOperand;
	this.rightOperand = rightOperand;
	this.negated = negated;
	this.caseInsensitive = caseInsensitive;
    }

    @Override
    public ua.com.fielden.platform.eql.s2.elements.LikeTest transform() {
	return new ua.com.fielden.platform.eql.s2.elements.LikeTest(leftOperand.transform(), rightOperand.transform(), negated, caseInsensitive);
    }

    @Override
    public boolean ignore() {
	return leftOperand.ignore() || rightOperand.ignore();
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
	if (!(obj instanceof LikeTest)) {
	    return false;
	}
	final LikeTest other = (LikeTest) obj;
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
    protected List<IElement> getCollection() {
	return new ArrayList<IElement>(){{add(leftOperand); add(rightOperand);}};
    }
}