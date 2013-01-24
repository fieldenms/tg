package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.Date;

import ua.com.fielden.platform.entity.query.generation.DbVersion;

public class LowerCaseOf extends SingleOperandFunction {
    public LowerCaseOf(final ISingleOperand operand, final DbVersion dbVersion) {
	super(dbVersion, operand);
    }

    @Override
    public String sql() {
	final StringBuffer sb = new StringBuffer();
	sb.append("LOWER(");
	if (Date.class.equals(getOperand().type())) {
	    sb.append("TO_CHAR(" + getOperand().sql() + ", 'YYYY-MM-DD HH24:MI:SS')");
	} else if (String.class.equals(getOperand().type())) {
	    sb.append(getOperand().sql());
	} else {
	    sb.append("CAST(" + getOperand().sql() + " AS VARCHAR(255))");
	}
	sb.append(")");

	return sb.toString();
    }
}