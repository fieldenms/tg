package ua.com.fielden.platform.eql.stage1.elements;

import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage2.elements.IfNull2;
import ua.com.fielden.platform.eql.stage2.elements.ISingleOperand2;

public class IfNull1 extends TwoOperandsFunction1<IfNull2> {

    public IfNull1(final ISingleOperand1<? extends ISingleOperand2> operand1, final ISingleOperand1<? extends ISingleOperand2> operand2) {
        super(operand1, operand2);
    }

    @Override
    public IfNull2 transform(final PropsResolutionContext resolutionContext) {
        return new IfNull2(getOperand1().transform(resolutionContext), getOperand2().transform(resolutionContext));
    }
}