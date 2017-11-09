package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.DayOf2;
import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;

public class DayOf1 extends SingleOperandFunction1<DayOf2> {
    public DayOf1(final ISingleOperand1<? extends ISingleOperand2> operand) {
        super(operand);
    }

    @Override
    public DayOf2 transform(final TransformatorToS2 resolver) {
        return new DayOf2(getOperand().transform(resolver));
    }
}