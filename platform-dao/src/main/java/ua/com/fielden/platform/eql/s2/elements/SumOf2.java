package ua.com.fielden.platform.eql.s2.elements;

import java.math.BigDecimal;


public class SumOf2 extends SingleOperandFunction2 {
    private final boolean distinct;
    public SumOf2(final ISingleOperand2 operand, final boolean distinct) {
	super(operand);
	this.distinct = distinct;
    }
    @Override
    public Class type() {
	return BigDecimal.class;
    }
}