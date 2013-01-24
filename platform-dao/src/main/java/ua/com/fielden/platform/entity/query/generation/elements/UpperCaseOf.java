package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.Date;

import ua.com.fielden.platform.entity.query.generation.DbVersion;

public class UpperCaseOf extends SingleOperandFunction {
    public UpperCaseOf(final ISingleOperand operand, final DbVersion dbVersion) {
	super(dbVersion, operand);
    }

    public String sql() {
	final StringBuffer sb = new StringBuffer();
	sb.append("UPPER(");
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