package ua.com.fielden.platform.eql.stage2.elements.functions;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

abstract class SingleOperandFunction2<S3 extends ISingleOperand3> extends AbstractFunction2<S3> {

    public final ISingleOperand2<?> operand;

    public SingleOperandFunction2(final ISingleOperand2<?> operand) {
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

        if (!(obj instanceof SingleOperandFunction2)) {
            return false;
        }
        
        final SingleOperandFunction2 other = (SingleOperandFunction2) obj;
        
        return Objects.equals(operand, other.operand);
    }
}