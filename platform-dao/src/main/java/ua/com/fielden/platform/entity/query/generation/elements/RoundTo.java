package ua.com.fielden.platform.entity.query.generation.elements;

import ua.com.fielden.platform.entity.query.generation.DbVersion;


public class RoundTo extends TwoOperandsFunction {

    public RoundTo(final ISingleOperand operand1, final ISingleOperand operand2, final DbVersion dbVersion) {
	super(dbVersion, operand1, operand2);
    }

    @Override
    public String sql() {
	return "ROUND(" + getOperand1().sql() + ", " + getOperand2().sql() + ")";
    }
}