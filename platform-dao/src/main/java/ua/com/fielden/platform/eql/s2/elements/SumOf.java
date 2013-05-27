package ua.com.fielden.platform.eql.s2.elements;


public class SumOf extends SingleOperandFunction {
    private final boolean distinct;
    public SumOf(final ISingleOperand2 operand, final boolean distinct) {
	super(operand);
	this.distinct = distinct;
    }
}