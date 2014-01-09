package ua.com.fielden.platform.entity.query.generation.elements;

import ua.com.fielden.platform.entity.query.DbVersion;

public class UpperCaseOf extends SingleOperandFunction {
    public UpperCaseOf(final ISingleOperand operand, final DbVersion dbVersion) {
	super(dbVersion, operand);
    }

    public String sql() {
	final StringBuffer sb = new StringBuffer();

	sb.append("UPPER(");
	sb.append(getConvertToStringSql(getOperand()));
	sb.append(")");

	return sb.toString();
    }
}