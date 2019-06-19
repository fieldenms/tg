package ua.com.fielden.platform.eql.stage2.elements.operands;

import com.google.common.base.Objects;

import ua.com.fielden.platform.entity.query.fluent.enums.ArithmeticalOperator;

public class CompoundSingleOperand2 {
    public final ISingleOperand2 operand;
    public final ArithmeticalOperator operator;

    public CompoundSingleOperand2(final ISingleOperand2 operand, final ArithmeticalOperator operator) {
        this.operand = operand;
        this.operator = operator;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((operand == null) ? 0 : operand.hashCode());
        result = prime * result + ((operator == null) ? 0 : operator.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof CompoundSingleOperand2)) {
            return false;
        }
        
        final CompoundSingleOperand2 other = (CompoundSingleOperand2) obj;
        
        return Objects.equal(operand, other.operand) &&
                Objects.equal(operator, other.operator);
    }
}
