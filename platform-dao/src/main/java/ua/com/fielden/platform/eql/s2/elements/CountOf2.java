package ua.com.fielden.platform.eql.s2.elements;



public class CountOf2 extends SingleOperandFunction2 {
    private final boolean distinct;
    public CountOf2(final ISingleOperand2 operand, final boolean distinct) {
	super(operand);
	this.distinct = distinct;
    }
}