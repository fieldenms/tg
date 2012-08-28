package ua.com.fielden.platform.entity.query.generation.elements;

import ua.com.fielden.platform.entity.query.generation.DbVersion;


public class YearOf extends SingleOperandFunction {

    public YearOf(final ISingleOperand operand, final DbVersion dbVersion) {
	super(dbVersion, operand);
    }

    @Override
    public String sql() {
	switch (getDbVersion()) {
	case H2:
	case MSSQL:
	    return "YEAR(" + getOperand().sql() + ")";
	case POSTGRESQL:
	    return "CAST(EXTRACT(YEAR FROM " + getOperand().sql() + ") AS INT)";
	default:
	    throw new IllegalStateException("Function [" + getClass().getSimpleName() +"] is not yet implemented for RDBMS [" + getDbVersion() + "]!");
	}

    }
}