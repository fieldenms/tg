package ua.com.fielden.platform.eql.s1.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;
import ua.com.fielden.platform.eql.s2.elements.LikeTest2;


public class LikeTest1 extends AbstractCondition1<LikeTest2> {
    private final ISingleOperand1<? extends ISingleOperand2> leftOperand;
    private final ISingleOperand1<? extends ISingleOperand2> rightOperand;
    private final boolean negated;
    private final boolean caseInsensitive;

    public LikeTest1(final ISingleOperand1<? extends ISingleOperand2> leftOperand, final ISingleOperand1<? extends ISingleOperand2> rightOperand, final boolean negated, final boolean caseInsensitive) {
	this.leftOperand = leftOperand;
	this.rightOperand = rightOperand;
	this.negated = negated;
	this.caseInsensitive = caseInsensitive;
    }

    @Override
    public LikeTest2 transform(final TransformatorToS2 resolver) {
	return new LikeTest2(leftOperand.transform(resolver), rightOperand.transform(resolver), negated, caseInsensitive);
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
	if (!(obj instanceof LikeTest1)) {
	    return false;
	}
	final LikeTest1 other = (LikeTest1) obj;
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
    protected List<IElement1> getCollection() {
	return new ArrayList<IElement1>(){{add(leftOperand); add(rightOperand);}};
    }
}