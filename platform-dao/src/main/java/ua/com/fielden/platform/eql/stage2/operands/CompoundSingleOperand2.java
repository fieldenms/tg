package ua.com.fielden.platform.eql.stage2.operands;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.fluent.enums.ArithmeticalOperator;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public class CompoundSingleOperand2 {
    public final ISingleOperand2<? extends ISingleOperand3> operand;
    public final ArithmeticalOperator operator;

    public CompoundSingleOperand2(final ISingleOperand2<? extends ISingleOperand3> operand, final ArithmeticalOperator operator) {
        this.operand = operand;
        this.operator = operator;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + operand.hashCode();
        result = prime * result + operator.hashCode();
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
        
        return Objects.equals(operand, other.operand) &&
                Objects.equals(operator, other.operator);
    }
}
