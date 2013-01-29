package ua.com.fielden.platform.entity.query.generation.elements;

import ua.com.fielden.platform.entity.query.generation.DbVersion;

public class LowerCaseOf extends SingleOperandFunction {
    public LowerCaseOf(final ISingleOperand operand, final DbVersion dbVersion) {
	super(dbVersion, operand);
    }

    @Override
    public String sql() {
	final StringBuffer sb = new StringBuffer();

	sb.append("LOWER(");
	sb.append(getConvertToStringSql(getOperand()));
	sb.append(")");

	return sb.toString();
    }
}