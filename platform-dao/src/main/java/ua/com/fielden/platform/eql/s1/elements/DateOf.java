package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;



public class DateOf extends SingleOperandFunction<ua.com.fielden.platform.eql.s2.elements.DateOf> {

    public DateOf(final ISingleOperand<? extends ISingleOperand2> operand) {
	super(operand);
    }

    @Override
    public ua.com.fielden.platform.eql.s2.elements.DateOf transform() {
	return new ua.com.fielden.platform.eql.s2.elements.DateOf(getOperand().transform());
    }
}