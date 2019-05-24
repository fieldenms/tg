package ua.com.fielden.platform.eql.stage2.elements;

import ua.com.fielden.platform.eql.stage2.elements.ISingleOperand2;

public class UpperCaseOf2 extends SingleOperandFunction2 {
    public UpperCaseOf2(final ISingleOperand2 operand) {
        super(operand);
    }

    @Override
    public String type() {
        return String.class.getName();
    }
}