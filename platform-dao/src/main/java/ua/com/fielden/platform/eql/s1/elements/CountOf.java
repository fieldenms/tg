package ua.com.fielden.platform.eql.s1.elements;



public class CountOf extends SingleOperandFunction {
    private final boolean distinct;
    public CountOf(final ISingleOperand operand, final boolean distinct) {
	super(operand);
	this.distinct = distinct;
    }
}