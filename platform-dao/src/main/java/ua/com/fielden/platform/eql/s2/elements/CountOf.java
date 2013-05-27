package ua.com.fielden.platform.eql.s2.elements;



public class CountOf extends SingleOperandFunction {
    private final boolean distinct;
    public CountOf(final ISingleOperand2 operand, final boolean distinct) {
	super(operand);
	this.distinct = distinct;
    }
}