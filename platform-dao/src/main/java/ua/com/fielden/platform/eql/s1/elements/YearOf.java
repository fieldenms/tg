package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;




public class YearOf extends SingleOperandFunction<ua.com.fielden.platform.eql.s2.elements.YearOf> {

    public YearOf(final ISingleOperand<? extends ISingleOperand2> operand) {
	super(operand);
    }

    @Override
    public ua.com.fielden.platform.eql.s2.elements.YearOf transform() {
	return new ua.com.fielden.platform.eql.s2.elements.YearOf(getOperand().transform());
    }
}