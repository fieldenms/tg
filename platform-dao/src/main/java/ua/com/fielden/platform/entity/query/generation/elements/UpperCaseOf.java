package ua.com.fielden.platform.entity.query.generation.elements;

import ua.com.fielden.platform.entity.query.generation.DbVersion;

public class UpperCaseOf extends SingleOperandFunction {
    public UpperCaseOf(final ISingleOperand operand, final DbVersion dbVersion) {
	super(dbVersion, operand);
    }

    @Override
    public String sql() {
	return "UPPER(" + getOperand().sql() + ")";
    }
}