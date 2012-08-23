package ua.com.fielden.platform.entity.query.generation.elements;

import ua.com.fielden.platform.entity.query.generation.DbVersion;


public class MinuteOf extends SingleOperandFunction {

    public MinuteOf(final ISingleOperand operand, final DbVersion dbVersion) {
	super(dbVersion, operand);
    }

    @Override
    public String sql() {
	return "MINUTE(" + getOperand().sql() + ")";
    }
}