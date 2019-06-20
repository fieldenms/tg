package ua.com.fielden.platform.eql.stage1.elements.functions;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

abstract class TwoOperandsFunction1<S2 extends ISingleOperand2> extends AbstractFunction1<S2> {
    public final ISingleOperand1<? extends ISingleOperand2> operand1;
    public final ISingleOperand1<? extends ISingleOperand2> operand2;

    public TwoOperandsFunction1(final ISingleOperand1<? extends ISingleOperand2> operand1, final ISingleOperand1<? extends ISingleOperand2> operand2) {
        this.operand1 = operand1;
        this.operand2 = operand2;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((operand1 == null) ? 0 : operand1.hashCode());
        result = prime * result + ((operand2 == null) ? 0 : operand2.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof TwoOperandsFunction1)) {
            return false;
        }
        
        final TwoOperandsFunction1<S2> other = (TwoOperandsFunction1<S2>) obj;
        
        return Objects.equals(operand1, other.operand1) && Objects.equals(operand2, other.operand2);
    }
}