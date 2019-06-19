package ua.com.fielden.platform.eql.stage2.elements.conditions;

import com.google.common.base.Objects;

import ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class ComparisonTest2 extends AbstractCondition2 {
    public final ISingleOperand2 leftOperand;
    public final ISingleOperand2 rightOperand;
    public final ComparisonOperator operator;

    public ComparisonTest2(final ISingleOperand2 leftOperand, final ComparisonOperator operator, final ISingleOperand2 rightOperand) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.operator = operator;
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

        if (!(obj instanceof ComparisonTest2)) {
            return false;
        }
        
        final ComparisonTest2 other = (ComparisonTest2) obj;
        
        return Objects.equal(leftOperand, other.leftOperand) &&
                Objects.equal(rightOperand, other.rightOperand) &&
                Objects.equal(operator, other.operator);
   }
}