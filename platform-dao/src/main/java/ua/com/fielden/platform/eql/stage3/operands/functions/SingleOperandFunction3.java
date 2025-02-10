package ua.com.fielden.platform.eql.stage3.operands.functions;

import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.utils.ToString;

import java.util.Objects;

abstract class SingleOperandFunction3 extends AbstractFunction3 {

    public final ISingleOperand3 operand;

    public SingleOperandFunction3(final ISingleOperand3 operand, final PropType type) {
        super(type);
        this.operand = operand;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + operand.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
               || obj instanceof SingleOperandFunction3 that
                  && Objects.equals(operand, that.operand)
                  && super.equals(obj);
    }

    @Override
    protected ToString addToString(final ToString toString) {
        return super.addToString(toString).add("operand", operand);
    }

}
