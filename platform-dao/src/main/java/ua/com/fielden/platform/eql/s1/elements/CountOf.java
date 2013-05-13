package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.entity.query.generation.DbVersion;


public class CountOf extends SingleOperandFunction {
    private final boolean distinct;
    public CountOf(final ISingleOperand operand, final boolean distinct, final DbVersion dbVersion) {
	super(dbVersion, operand);
	this.distinct = distinct;
    }
}