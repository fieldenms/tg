package ua.com.fielden.platform.eql.s1.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.ComparisonOperator;
import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.ComparisonTest2;
import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;


public class ComparisonTest1 extends AbstractCondition1<ComparisonTest2> {
    private final ISingleOperand1<? extends ISingleOperand2> leftOperand;
    private final ISingleOperand1<? extends ISingleOperand2> rightOperand;
    private final ComparisonOperator operator;

    @Override
    public String toString() {
        return leftOperand + " " + operator + " " + rightOperand;
    }

    public ComparisonTest1(final ISingleOperand1<? extends ISingleOperand2> leftOperand, final ComparisonOperator operator, final ISingleOperand1<? extends ISingleOperand2> rightOperand) {
	this.leftOperand = leftOperand;
	this.rightOperand = rightOperand;
	this.operator = operator;
    }

    public ComparisonTest2 transform(final TransformatorToS2 resolver) {
	return new ComparisonTest2(leftOperand.transform(resolver), operator, rightOperand.transform(resolver));
    }

    @Override
    public boolean ignore() {
	return leftOperand.ignore() || rightOperand.ignore();
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((leftOperand == null) ? 0 : leftOperand.hashCode());
	result = prime * result + ((operator == null) ? 0 : operator.hashCode());
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
	if (!(obj instanceof ComparisonTest1)) {
	    return false;
	}
	final ComparisonTest1 other = (ComparisonTest1) obj;
	if (leftOperand == null) {
	    if (other.leftOperand != null) {
		return false;
	    }
	} else if (!leftOperand.equals(other.leftOperand)) {
	    return false;
	}
	if (operator != other.operator) {
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