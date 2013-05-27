package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;


public class SumOf extends SingleOperandFunction<ua.com.fielden.platform.eql.s2.elements.SumOf> {
    private final boolean distinct;
    public SumOf(final ISingleOperand<? extends ISingleOperand2> operand, final boolean distinct) {
	super(operand);
	this.distinct = distinct;
    }

    @Override
    public ua.com.fielden.platform.eql.s2.elements.SumOf transform() {
	return new ua.com.fielden.platform.eql.s2.elements.SumOf(getOperand().transform(), distinct);
    }
}