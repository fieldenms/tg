package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.ComparisonOperator;


public class ComparisonTestModel implements ICondition {
    private final ISingleOperand leftOperand;
    private final ISingleOperand rightOperand;
    private final ComparisonOperator operator;

    @Override
    public String toString() {
        return leftOperand + " " + operator + " " + rightOperand;
    }

    @Override
    public String sql() {
	return leftOperand.sql() + " " + operator + " " + rightOperand.sql();
    }

    public ComparisonTestModel(final ISingleOperand leftOperand, final ComparisonOperator operator, final ISingleOperand rightOperand) {
	this.leftOperand = leftOperand;
	this.rightOperand = rightOperand;
	this.operator = operator;
    }

    @Override
    public List<EntProp> getProps() {
	final List<EntProp> result = new ArrayList<EntProp>();
	result.addAll(leftOperand.getProps());
	result.addAll(rightOperand.getProps());
	return result;
    }

    @Override
    public List<EntQuery> getSubqueries() {
	final List<EntQuery> result = new ArrayList<EntQuery>();
	result.addAll(leftOperand.getSubqueries());
	result.addAll(rightOperand.getSubqueries());
	return result;
    }

    @Override
    public List<EntValue> getValues() {
	final List<EntValue> result = new ArrayList<EntValue>();
	result.addAll(leftOperand.getValues());
	result.addAll(rightOperand.getValues());
	return result;
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
	if (!(obj instanceof ComparisonTestModel)) {
	    return false;
	}
	final ComparisonTestModel other = (ComparisonTestModel) obj;
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
}