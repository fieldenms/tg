package ua.com.fielden.platform.eql.stage1.elements.functions;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

abstract class SingleOperandFunction1<S2 extends ISingleOperand2> extends AbstractFunction1<S2> {

    public final ISingleOperand1<? extends ISingleOperand2> operand;

    public SingleOperandFunction1(final ISingleOperand1<? extends ISingleOperand2> operand) {
        this.operand = operand;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((operand == null) ? 0 : operand.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof SingleOperandFunction1)) {
            return false;
        }
        
        final SingleOperandFunction1<S2> other = (SingleOperandFunction1<S2>) obj;
        
        return Objects.equals(operand, other.operand);
    }
}