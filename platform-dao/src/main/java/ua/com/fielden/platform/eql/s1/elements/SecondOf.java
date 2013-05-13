package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.entity.query.generation.DbVersion;


public class SecondOf extends SingleOperandFunction {

    public SecondOf(final ISingleOperand operand, final DbVersion dbVersion) {
	super(dbVersion, operand);
    }
}