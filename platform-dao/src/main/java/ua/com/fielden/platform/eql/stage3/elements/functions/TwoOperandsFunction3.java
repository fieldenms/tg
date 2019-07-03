package ua.com.fielden.platform.eql.stage3.elements.functions;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

abstract class TwoOperandsFunction3 extends AbstractFunction3 {
    public final ISingleOperand3 operand1;
    public final ISingleOperand3 operand2;

    public TwoOperandsFunction3(final ISingleOperand3 operand1, final ISingleOperand3 operand2) {
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

        if (!(obj instanceof TwoOperandsFunction3)) {
            return false;
        }
        
        final TwoOperandsFunction3 other = (TwoOperandsFunction3) obj;
        
        return Objects.equals(operand1, other.operand1) && Objects.equals(operand2, other.operand2);
    }
}