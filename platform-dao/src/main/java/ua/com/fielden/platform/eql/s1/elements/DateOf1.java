package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.DateOf2;
import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;



public class DateOf1 extends SingleOperandFunction1<DateOf2> {

    public DateOf1(final ISingleOperand1<? extends ISingleOperand2> operand) {
	super(operand);
    }

    @Override
    public DateOf2 transform(final TransformatorToS2 resolver) {
	return new DateOf2(getOperand().transform(resolver));
    }
}