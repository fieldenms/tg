package ua.com.fielden.platform.eql.stage3.elements.functions;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

abstract class SingleOperandFunction3 implements ISingleOperand3 {

    public final ISingleOperand3 operand;

    public SingleOperandFunction3(final ISingleOperand3 operand) {
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

        if (!(obj instanceof SingleOperandFunction3)) {
            return false;
        }
        
        final SingleOperandFunction3 other = (SingleOperandFunction3) obj;
        
        return Objects.equals(operand, other.operand);
    }
}