package ua.com.fielden.platform.eql.stage1.elements;

import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage2.elements.SecondOf2;
import ua.com.fielden.platform.eql.stage2.elements.ISingleOperand2;

public class SecondOf1 extends SingleOperandFunction1<SecondOf2> {

    public SecondOf1(final ISingleOperand1<? extends ISingleOperand2> operand) {
        super(operand);
    }

    @Override
    public SecondOf2 transform(final PropsResolutionContext resolutionContext) {
        return new SecondOf2(getOperand().transform(resolutionContext));
    }
}