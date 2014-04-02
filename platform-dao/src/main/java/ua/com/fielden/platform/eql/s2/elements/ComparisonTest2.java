package ua.com.fielden.platform.eql.s2.elements;

import ua.com.fielden.platform.entity.query.fluent.ComparisonOperator;

public class ComparisonTest2 extends AbstractCondition2 {
    private final ISingleOperand2 leftOperand;
    private final ISingleOperand2 rightOperand;
    private final ComparisonOperator operator;

    @Override
    public String toString() {
        return leftOperand + " " + operator + " " + rightOperand;
    }

    @Override
    public boolean ignore() {
        return leftOperand.ignore() || rightOperand.ignore();
    }

    public ComparisonTest2(final ISingleOperand2 leftOperand, final ComparisonOperator operator, final ISingleOperand2 rightOperand) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.operator = operator;
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
        if (!(obj instanceof ComparisonTest2)) {
            return false;
        }
        final ComparisonTest2 other = (ComparisonTest2) obj;
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