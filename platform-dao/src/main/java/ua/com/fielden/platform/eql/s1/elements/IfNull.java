package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.entity.query.generation.DbVersion;


public class IfNull extends TwoOperandsFunction {

    public IfNull(final ISingleOperand operand1, final ISingleOperand operand2, final DbVersion dbVersion) {
	super(dbVersion, operand1, operand2);
    }
}