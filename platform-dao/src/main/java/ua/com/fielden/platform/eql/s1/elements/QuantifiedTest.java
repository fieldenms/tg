package ua.com.fielden.platform.eql.s1.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.ComparisonOperator;
import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;

public class QuantifiedTest extends AbstractCondition<ua.com.fielden.platform.eql.s2.elements.QuantifiedTest> {
    private final ISingleOperand<ISingleOperand2> leftOperand;
    private final EntQuery rightOperand;
    private final Quantifier quantifier;
    private final ComparisonOperator operator;

    public QuantifiedTest(final ISingleOperand<ISingleOperand2> leftOperand, final ComparisonOperator operator, final Quantifier quantifier, final EntQuery rightOperand) {
	this.leftOperand = leftOperand;
	this.rightOperand = rightOperand;
	this.operator = operator;
	this.quantifier = quantifier;
    }

    @Override
    public ua.com.fielden.platform.eql.s2.elements.QuantifiedTest transform(TransformatorToS2 resolver) {
	return new ua.com.fielden.platform.eql.s2.elements.QuantifiedTest(leftOperand.transform(null), operator, quantifier, rightOperand.transform(null));
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
	result = prime * result + ((operator == null) ? 0 : operator.hashCode());
	result = prime * result + ((quantifier == null) ? 0 : quantifier.hashCode());
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
	if (!(obj instanceof QuantifiedTest)) {
	    return false;
	}
	final QuantifiedTest other = (QuantifiedTest) obj;
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
	if (quantifier != other.quantifier) {
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