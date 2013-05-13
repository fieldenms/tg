package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.entity.query.generation.DbVersion;


public class MaxOf extends SingleOperandFunction {

    public MaxOf(final ISingleOperand operand, final DbVersion dbVersion) {
	super(dbVersion, operand);
    }
}