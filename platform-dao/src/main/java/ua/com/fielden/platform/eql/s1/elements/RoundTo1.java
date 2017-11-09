package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;
import ua.com.fielden.platform.eql.s2.elements.RoundTo2;

public class RoundTo1 extends TwoOperandsFunction1<RoundTo2> {

    public RoundTo1(final ISingleOperand1<? extends ISingleOperand2> operand1, final ISingleOperand1<? extends ISingleOperand2> operand2) {
        super(operand1, operand2);
    }

    @Override
    public RoundTo2 transform(final TransformatorToS2 resolver) {
        return new RoundTo2(getOperand1().transform(resolver), getOperand2().transform(resolver));
    }
}