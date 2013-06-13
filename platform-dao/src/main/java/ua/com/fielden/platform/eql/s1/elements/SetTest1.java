package ua.com.fielden.platform.eql.s1.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.ISetOperand2;
import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;
import ua.com.fielden.platform.eql.s2.elements.SetTest2;

public class SetTest1 extends AbstractCondition1<SetTest2> {
    private final ISingleOperand1<ISingleOperand2> leftOperand;
    private final ISetOperand1<ISetOperand2> rightOperand;
    private final boolean negated;

    public SetTest1(final ISingleOperand1<ISingleOperand2> leftOperand, final boolean negated, final ISetOperand1<ISetOperand2> rightOperand) {
	this.leftOperand = leftOperand;
	this.rightOperand = rightOperand;
	this.negated = negated;
    }

    @Override
    public SetTest2 transform(final TransformatorToS2 resolver) {
	return new SetTest2(leftOperand.transform(resolver), negated, rightOperand.transform(resolver));
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
	if (!(obj instanceof SetTest1)) {
	    return false;
	}
	final SetTest1 other = (SetTest1) obj;
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