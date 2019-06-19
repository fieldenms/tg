package ua.com.fielden.platform.eql.stage2.elements.functions;

import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class IfNull2 extends TwoOperandsFunction2 {

    public IfNull2(final ISingleOperand2 operand1, final ISingleOperand2 operand2) {
        super(operand1, operand2);
    }

    @Override
    public Class type() {
        // TODO EQL
        return operand1.type();
    }
}