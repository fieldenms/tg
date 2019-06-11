package ua.com.fielden.platform.eql.stage2.elements.functions;

import java.sql.Date;

import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class DateOf2 extends SingleOperandFunction2 {

    public DateOf2(final ISingleOperand2 operand) {
        super(operand);
    }

    @Override
    public Class type() {
        // TODO EQL
        return Date.class;
    }
}