package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;



public class MinuteOf extends SingleOperandFunction<ua.com.fielden.platform.eql.s2.elements.MinuteOf> {

    public MinuteOf(final ISingleOperand<? extends ISingleOperand2> operand) {
	super(operand);
    }

    @Override
    public ua.com.fielden.platform.eql.s2.elements.MinuteOf transform() {
	return new ua.com.fielden.platform.eql.s2.elements.MinuteOf(getOperand().transform());
    }
}