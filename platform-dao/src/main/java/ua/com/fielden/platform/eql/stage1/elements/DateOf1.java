package ua.com.fielden.platform.eql.stage1.elements;

import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage2.elements.DateOf2;
import ua.com.fielden.platform.eql.stage2.elements.ISingleOperand2;

public class DateOf1 extends SingleOperandFunction1<DateOf2> {

    public DateOf1(final ISingleOperand1<? extends ISingleOperand2> operand) {
        super(operand);
    }

    @Override
    public DateOf2 transform(final PropsResolutionContext resolver) {
        return new DateOf2(getOperand().transform(resolver));
    }
}