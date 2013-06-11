package ua.com.fielden.platform.eql.s2.elements;


public class UpperCaseOf2 extends SingleOperandFunction2 {
    public UpperCaseOf2(final ISingleOperand2 operand) {
	super(operand);
    }

    @Override
    public Class type() {
	return String.class;
    }
}