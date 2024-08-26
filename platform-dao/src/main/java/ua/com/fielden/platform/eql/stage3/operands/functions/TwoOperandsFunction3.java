package ua.com.fielden.platform.eql.stage3.operands.functions;

import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

import java.util.Objects;

abstract class TwoOperandsFunction3 extends AbstractFunction3 {
    public final ISingleOperand3 operand1;
    public final ISingleOperand3 operand2;

    public TwoOperandsFunction3(final ISingleOperand3 operand1, final ISingleOperand3 operand2, final PropType type) {
        super(type);
        this.operand1 = operand1;
        this.operand2 = operand2;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + operand1.hashCode();
        result = prime * result + operand2.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
               || obj instanceof TwoOperandsFunction3 that
                  && Objects.equals(operand1, that.operand1)
                  && Objects.equals(operand2, that.operand2)
                  && super.equals(that);
    }
}
