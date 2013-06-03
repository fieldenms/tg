package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.HourOf2;
import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;



public class HourOf1 extends SingleOperandFunction1<HourOf2> {

    public HourOf1(final ISingleOperand1<? extends ISingleOperand2> operand) {
	super(operand);
    }

    @Override
    public HourOf2 transform(final TransformatorToS2 resolver) {
	return new HourOf2(getOperand().transform(resolver));
    }
}