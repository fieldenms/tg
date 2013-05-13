package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.entity.query.generation.DbVersion;


public class RoundTo extends TwoOperandsFunction {

    public RoundTo(final ISingleOperand operand1, final ISingleOperand operand2, final DbVersion dbVersion) {
	super(dbVersion, operand1, operand2);
    }
}