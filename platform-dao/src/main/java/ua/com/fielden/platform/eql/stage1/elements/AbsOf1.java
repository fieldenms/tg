package ua.com.fielden.platform.eql.stage1.elements;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.stage2.elements.AbsOf2;
import ua.com.fielden.platform.eql.stage2.elements.ISingleOperand2;

public class AbsOf1 extends SingleOperandFunction1<AbsOf2> {

    public AbsOf1(final ISingleOperand1<? extends ISingleOperand2> operand) {
        super(operand);
    }

    @Override
    public AbsOf2 transform(final TransformatorToS2 resolver) {
        return new AbsOf2(getOperand().transform(resolver));
    }
}