package ua.com.fielden.platform.eql.stage3.operands.functions;

import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.utils.ToString;

public abstract class TwoOperandsFunction3 extends AbstractFunction3 {
    public final ISingleOperand3 operand1;
    public final ISingleOperand3 operand2;

    public TwoOperandsFunction3(final ISingleOperand3 operand1, final ISingleOperand3 operand2, final PropType type) {
        super(type);
        this.operand1 = operand1;
        this.operand2 = operand2;
    }

    @Override
    protected ToString addToString(final ToString toString) {
        return super.addToString(toString)
                .add("operand1", operand1)
                .add("operand2", operand2);
    }

}
