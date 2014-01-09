package ua.com.fielden.platform.entity.query.generation.elements;

import ua.com.fielden.platform.entity.query.DbVersion;


public class DateOf extends SingleOperandFunction {

    public DateOf(final ISingleOperand operand, final DbVersion dbVersion) {
	super(dbVersion, operand);
    }

    @Override
    public String sql() {
	switch (getDbVersion()) {
	case H2:
	    return "CAST(" + getOperand().sql() + " AS DATE)";
	case MSSQL:
	    return "DATEADD(dd, DATEDIFF(dd, 0, " + getOperand().sql() + "), 0)";
	case POSTGRESQL:
	    return "DATE_TRUNC('day', " + getOperand().sql() + ")";
	default:
	    throw new IllegalStateException("Function [" + getClass().getSimpleName() +"] is not yet implemented for RDBMS [" + getDbVersion() + "]!");
	}
    }
}