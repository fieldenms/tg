package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;



public class MinOf extends SingleOperandFunction<ua.com.fielden.platform.eql.s2.elements.MinOf> {

    public MinOf(final ISingleOperand<? extends ISingleOperand2> operand) {
	super(operand);
    }

    @Override
    public ua.com.fielden.platform.eql.s2.elements.MinOf transform() {
	return new ua.com.fielden.platform.eql.s2.elements.MinOf(getOperand().transform());
    }
}