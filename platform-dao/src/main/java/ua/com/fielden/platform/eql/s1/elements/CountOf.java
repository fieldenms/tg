package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;



public class CountOf extends SingleOperandFunction<ua.com.fielden.platform.eql.s2.elements.CountOf> {
    private final boolean distinct;
    public CountOf(final ISingleOperand<? extends ISingleOperand2> operand, final boolean distinct) {
	super(operand);
	this.distinct = distinct;
    }

    @Override
    public ua.com.fielden.platform.eql.s2.elements.CountOf transform() {
	return new ua.com.fielden.platform.eql.s2.elements.CountOf(getOperand().transform(), distinct);
    }
}