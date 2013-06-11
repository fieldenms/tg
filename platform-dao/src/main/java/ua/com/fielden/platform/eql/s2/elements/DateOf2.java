package ua.com.fielden.platform.eql.s2.elements;

import java.sql.Date;



public class DateOf2 extends SingleOperandFunction2 {

    public DateOf2(final ISingleOperand2 operand) {
	super(operand);
    }

    @Override
    public Class type() {
	// TODO EQL
	return Date.class;
    }
}