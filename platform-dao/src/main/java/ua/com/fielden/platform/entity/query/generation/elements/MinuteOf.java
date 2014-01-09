package ua.com.fielden.platform.entity.query.generation.elements;

import ua.com.fielden.platform.entity.query.DbVersion;


public class MinuteOf extends SingleOperandFunction {

    public MinuteOf(final ISingleOperand operand, final DbVersion dbVersion) {
	super(dbVersion, operand);
    }

    @Override
    public String sql() {
	switch (getDbVersion()) {
	case H2:
	    return "MINUTE(" + getOperand().sql() + ")";
	case MSSQL:
	    return "DATEPART(mi, " + getOperand().sql() + ")";
	case POSTGRESQL:
	    return "CAST(EXTRACT(MINUTE FROM " + getOperand().sql() + ") AS INT)";
	default:
	    throw new IllegalStateException("Function [" + getClass().getSimpleName() +"] is not yet implemented for RDBMS [" + getDbVersion() + "]!");
	}
    }
}