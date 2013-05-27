package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;



public class HourOf extends SingleOperandFunction<ua.com.fielden.platform.eql.s2.elements.HourOf> {

    public HourOf(final ISingleOperand<? extends ISingleOperand2> operand) {
	super(operand);
    }

    @Override
    public ua.com.fielden.platform.eql.s2.elements.HourOf transform() {
	return new ua.com.fielden.platform.eql.s2.elements.HourOf(getOperand().transform());
    }
}