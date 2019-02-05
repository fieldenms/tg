package ua.com.fielden.platform.eql.stage1.elements;

import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage2.elements.DayOf2;
import ua.com.fielden.platform.eql.stage2.elements.ISingleOperand2;

public class DayOf1 extends SingleOperandFunction1<DayOf2> {
    public DayOf1(final ISingleOperand1<? extends ISingleOperand2> operand) {
        super(operand);
    }

    @Override
    public DayOf2 transform(final PropsResolutionContext resolutionContext) {
        return new DayOf2(getOperand().transform(resolutionContext));
    }
}