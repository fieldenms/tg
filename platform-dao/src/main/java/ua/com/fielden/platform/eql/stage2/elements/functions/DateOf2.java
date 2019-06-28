package ua.com.fielden.platform.eql.stage2.elements.functions;

import java.sql.Date;

import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.elements.functions.DateOf3;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class DateOf2 extends SingleOperandFunction2<DateOf3> {

    public DateOf2(final ISingleOperand2<? extends ISingleOperand3> operand) {
        super(operand);
    }

    @Override
    public Class type() {
        // TODO EQL
        return Date.class;
    }
}