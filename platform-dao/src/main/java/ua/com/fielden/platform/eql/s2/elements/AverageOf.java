package ua.com.fielden.platform.eql.s2.elements;



public class AverageOf extends SingleOperandFunction {
    private final boolean distinct;
    public AverageOf(final ISingleOperand2 operand, final boolean distinct) {
	super(operand);
	this.distinct = distinct;
    }
}