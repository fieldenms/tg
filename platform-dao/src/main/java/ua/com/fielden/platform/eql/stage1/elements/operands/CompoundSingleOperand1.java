package ua.com.fielden.platform.eql.stage1.elements.operands;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.fluent.enums.ArithmeticalOperator;
import ua.com.fielden.platform.eql.stage2.elements.ISingleOperand2;

public class CompoundSingleOperand1 {
    public final ISingleOperand1<? extends ISingleOperand2> operand;
    public final ArithmeticalOperator operator;

    public CompoundSingleOperand1(final ISingleOperand1<? extends ISingleOperand2> operand, final ArithmeticalOperator operator) {
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

        if (!(obj instanceof CompoundSingleOperand1)) {
            return false;
        }
        
        final CompoundSingleOperand1 other = (CompoundSingleOperand1) obj;
        
        return Objects.equals(operand, other.operand) && Objects.equals(operator, other.operator);
    }
}