package ua.com.fielden.platform.eql.s2.elements;



public class MonthOf2 extends SingleOperandFunction2 {

    public MonthOf2(final ISingleOperand2 operand) {
	super(operand);
    }

    @Override
    public Class type() {
	return Integer.class;
    }
}