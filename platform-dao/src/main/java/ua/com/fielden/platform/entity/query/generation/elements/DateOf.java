package ua.com.fielden.platform.entity.query.generation.elements;

import ua.com.fielden.platform.entity.query.generation.DbVersion;


public class DateOf extends SingleOperandFunction {

    public DateOf(final ISingleOperand operand, final DbVersion dbVersion) {
	super(dbVersion, operand);
    }

    @Override
    public String sql() {
	switch (getDbVersion()) {
	case H2:
	case MSSQL:
	case POSTGRESQL:
	    return "DATE_PART('day', " + getOperand().sql() + ")";
	default:
	    throw new IllegalStateException("Function [" + getClass().getSimpleName() +"] is not yet implemented for RDBMS [" + getDbVersion() + "]!");
	}
    }
}