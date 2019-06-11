package ua.com.fielden.platform.eql.stage2.elements.functions;

import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class UpperCaseOf2 extends SingleOperandFunction2 {
    public UpperCaseOf2(final ISingleOperand2 operand) {
        super(operand);
    }

    @Override
    public Class type() {
        return String.class;
    }
}