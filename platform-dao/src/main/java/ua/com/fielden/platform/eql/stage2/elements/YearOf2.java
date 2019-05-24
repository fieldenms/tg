package ua.com.fielden.platform.eql.stage2.elements;

import ua.com.fielden.platform.eql.stage2.elements.ISingleOperand2;

public class YearOf2 extends SingleOperandFunction2 {

    public YearOf2(final ISingleOperand2 operand) {
        super(operand);
    }

    @Override
    public String type() {
        return Integer.class.getName();
    }
}