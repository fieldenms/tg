package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;



public class MaxOf extends SingleOperandFunction<ua.com.fielden.platform.eql.s2.elements.MaxOf> {

    public MaxOf(final ISingleOperand<? extends ISingleOperand2> operand) {
	super(operand);
    }

    @Override
    public ua.com.fielden.platform.eql.s2.elements.MaxOf transform() {
	return new ua.com.fielden.platform.eql.s2.elements.MaxOf(getOperand().transform());
    }
}