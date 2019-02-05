package ua.com.fielden.platform.eql.stage1.elements;

import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage2.elements.MaxOf2;
import ua.com.fielden.platform.eql.stage2.elements.ISingleOperand2;

public class MaxOf1 extends SingleOperandFunction1<MaxOf2> {

    public MaxOf1(final ISingleOperand1<? extends ISingleOperand2> operand) {
        super(operand);
    }

    @Override
    public MaxOf2 transform(final PropsResolutionContext resolutionContext) {
        return new MaxOf2(getOperand().transform(resolutionContext));
    }
}