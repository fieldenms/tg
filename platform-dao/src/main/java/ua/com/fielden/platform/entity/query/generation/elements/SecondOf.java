package ua.com.fielden.platform.entity.query.generation.elements;

import ua.com.fielden.platform.entity.query.generation.DbVersion;


public class SecondOf extends SingleOperandFunction {

    public SecondOf(final ISingleOperand operand, final DbVersion dbVersion) {
	super(dbVersion, operand);
    }

    @Override
    public String sql() {
	return "SECOND(" + getOperand().sql() + ")";
    }
}