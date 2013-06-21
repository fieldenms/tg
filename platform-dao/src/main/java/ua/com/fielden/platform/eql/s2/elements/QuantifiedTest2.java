package ua.com.fielden.platform.eql.s2.elements;

import ua.com.fielden.platform.entity.query.fluent.ComparisonOperator;
import ua.com.fielden.platform.eql.meta.Quantifier;

public class QuantifiedTest2 extends AbstractCondition2 {
    private final ISingleOperand2 leftOperand;
    private final EntQuery2 rightOperand;
    private final Quantifier quantifier;
    private final ComparisonOperator operator;

    public QuantifiedTest2(final ISingleOperand2 leftOperand, final ComparisonOperator operator, final Quantifier quantifier, final EntQuery2 rightOperand) {
	this.leftOperand = leftOperand;
	this.rightOperand = rightOperand;
	this.operator = operator;
	this.quantifier = quantifier;
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
	if (!(obj instanceof QuantifiedTest2)) {
	    return false;
	}
	final QuantifiedTest2 other = (QuantifiedTest2) obj;
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
}