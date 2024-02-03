package ua.com.fielden.platform.eql.stage1.operands;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.fluent.enums.ArithmeticalOperator;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage2.operands.CompoundSingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;

public class CompoundSingleOperand1 {
    public final ISingleOperand1<? extends ISingleOperand2<?>> operand;
    public final ArithmeticalOperator operator;

    public CompoundSingleOperand1(final ISingleOperand1<? extends ISingleOperand2<?>> operand, final ArithmeticalOperator operator) {
        this.operand = operand;
        this.operator = operator;
    }
    
    public CompoundSingleOperand2 transform(final TransformationContextFromStage1To2 context) {
        return new CompoundSingleOperand2(operand.transform(context), operator);
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

        if (!(obj instanceof CompoundSingleOperand1)) {
            return false;
        }
        
        final CompoundSingleOperand1 other = (CompoundSingleOperand1) obj;
        
        return Objects.equals(operand, other.operand) && Objects.equals(operator, other.operator);
    }
}