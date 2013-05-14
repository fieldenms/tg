package ua.com.fielden.platform.eql.s1.elements;



public class AverageOf extends SingleOperandFunction {
    private final boolean distinct;
    public AverageOf(final ISingleOperand operand, final boolean distinct) {
	super(operand);
	this.distinct = distinct;
    }
}