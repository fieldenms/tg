package ua.com.fielden.platform.eql.stage1.elements;

import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage2.elements.MonthOf2;
import ua.com.fielden.platform.eql.stage2.elements.ISingleOperand2;

public class MonthOf1 extends SingleOperandFunction1<MonthOf2> {

    public MonthOf1(final ISingleOperand1<? extends ISingleOperand2> operand) {
        super(operand);
    }

    @Override
    public MonthOf2 transform(final PropsResolutionContext resolutionContext) {
        return new MonthOf2(getOperand().transform(resolutionContext));
    }
}