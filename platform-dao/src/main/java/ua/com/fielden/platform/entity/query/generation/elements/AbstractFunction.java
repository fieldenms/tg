package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.Date;

import ua.com.fielden.platform.entity.query.generation.DbVersion;

public abstract class AbstractFunction {
    private final DbVersion dbVersion;

    public AbstractFunction(final DbVersion dbVersion) {
	this.dbVersion = dbVersion;
    }

    protected DbVersion getDbVersion() {
	return dbVersion;
    }

    protected String getConvertToStringSql(final ISingleOperand operand) {
	switch (dbVersion) {
	case H2:
	    return getConvertToStringSqlForH2(operand);
	case MSSQL:
	    return getConvertToStringSqlForMsSql2005(operand);
	case POSTGRESQL:
	    return getConvertToStringSqlForPostgresql(operand);
	default:
	    throw new IllegalStateException("Function of converting value to string [" + getClass().getSimpleName() +"] is not yet implemented for RDBMS [" + dbVersion + "]!");
	}
    }

    private String getConvertToStringSqlForH2(final ISingleOperand operand) {
	if (Date.class.equals(operand.type())) {
	    return "FORMATDATETIME(" + operand.sql() + ", 'YYYY-MM-dd hh:mm:ss')";
	} else if (String.class.equals(operand.type())) {
	    return operand.sql();
	} else {
	    return "CAST(" + operand.sql() + " AS VARCHAR(255))";
	}
    }

    private String getConvertToStringSqlForMsSql2005(final ISingleOperand operand) {
	if (Date.class.equals(operand.type())) {
	    return "CONVERT(VARCHAR(19), " + operand.sql() + ", 120)";
	} else if (String.class.equals(operand.type())) {
	    return operand.sql();
	} else {
	    return "CAST(" + operand.sql() + " AS VARCHAR(255))";
	}
    }

    private String getConvertToStringSqlForPostgresql(final ISingleOperand operand) {
	if (Date.class.equals(operand.type())) {
	    return "TO_CHAR(" + operand.sql() + ", 'YYYY-MM-DD HH24:MI:SS')";
	} else if (String.class.equals(operand.type())) {
	    return operand.sql();
	} else {
	    return "CAST(" + operand.sql() + " AS VARCHAR(255))";
	}
    }
}