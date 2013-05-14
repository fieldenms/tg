package ua.com.fielden.platform.eql.s1.elements;


public class SumOf extends SingleOperandFunction {
    private final boolean distinct;
    public SumOf(final ISingleOperand operand, final boolean distinct) {
	super(operand);
	this.distinct = distinct;
    }
}