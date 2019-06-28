package ua.com.fielden.platform.eql.stage2.elements.functions;

import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.elements.functions.IfNull3;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class IfNull2 extends TwoOperandsFunction2<IfNull3> {

    public IfNull2(final ISingleOperand2<? extends ISingleOperand3> operand1, final ISingleOperand2<? extends ISingleOperand3> operand2) {
        super(operand1, operand2);
    }

    @Override
    public Class type() {
        // TODO EQL
        return operand1.type();
    }
}