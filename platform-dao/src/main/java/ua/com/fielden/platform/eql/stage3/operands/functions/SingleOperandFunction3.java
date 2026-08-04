package ua.com.fielden.platform.eql.stage3.operands.functions;

import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.utils.ToString;

abstract class SingleOperandFunction3 extends AbstractFunction3 {

    public final ISingleOperand3 operand;

    public SingleOperandFunction3(final ISingleOperand3 operand, final PropType type) {
        super(type);
        this.operand = operand;
    }
    
    @Override
    protected ToString addToString(final ToString toString) {
        return super.addToString(toString).add("operand", operand);
    }

}
