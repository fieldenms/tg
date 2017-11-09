package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;
import ua.com.fielden.platform.eql.s2.elements.MinuteOf2;

public class MinuteOf1 extends SingleOperandFunction1<MinuteOf2> {

    public MinuteOf1(final ISingleOperand1<? extends ISingleOperand2> operand) {
        super(operand);
    }

    @Override
    public MinuteOf2 transform(final TransformatorToS2 resolver) {
        return new MinuteOf2(getOperand().transform(resolver));
    }
}