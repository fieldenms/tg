package ua.com.fielden.platform.entity.query.generation.elements;

import ua.com.fielden.platform.entity.query.generation.DbVersion;

public class LowerCaseOf extends SingleOperandFunction {
    public LowerCaseOf(final ISingleOperand operand, final DbVersion dbVersion) {
	super(dbVersion, operand);
    }

    @Override
    public String sql() {
	return "LOWER(" + getOperand().sql() + ")";
    }
}