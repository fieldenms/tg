package ua.com.fielden.platform.entity.query.generation.elements;

import ua.com.fielden.platform.entity.query.fluent.DateIntervalUnit;
import ua.com.fielden.platform.entity.query.generation.DbVersion;


public class CountDateInterval extends TwoOperandsFunction {

    private DateIntervalUnit intervalUnit;

    public CountDateInterval(final DateIntervalUnit intervalUnit, final ISingleOperand operand1, final ISingleOperand operand2, final DbVersion dbVersion) {
	super(dbVersion, operand1, operand2);
	this.intervalUnit = intervalUnit;
    }

    @Override
    public String sql() {
	switch (getDbVersion()) {
	case H2:
	    return sqlForH2();
	case MSSQL:
	    return sqlForMsSql();
	case POSTGRESQL:
	    return sqlForPostgres();
	default:
	    throw new IllegalStateException("Function [" + getClass().getSimpleName() +"] is not yet implemented for RDBMS [" + getDbVersion() + "]!");
	}
    }

    private String sqlForPostgres() {
	switch (intervalUnit) {
	case HOUR:
	    return "EXTRACT(HOUR FROM (" + getOperand1().sql() + " - " + getOperand2().sql() + "))";
	case DAY:
	    return "EXTRACT(DAY FROM (" + getOperand1().sql() + " - " + getOperand2().sql() + "))";
	case MONTH:
	    return "EXTRACT(MONTH FROM (" + getOperand1().sql() + " - " + getOperand2().sql() + "))";
	case YEAR:
	    return "EXTRACT(YEAR FROM (" + getOperand1().sql() + " - " + getOperand2().sql() + "))";
	default:
	    throw new IllegalStateException("Unexpected interval unit: " + intervalUnit);
	}
    }

    private String sqlForH2() {
	switch (intervalUnit) {
	case HOUR:
	    return "DATEDIFF('HOUR', " + getOperand1().sql() + ", " + getOperand2().sql() + ")";
	case DAY:
	    return "DATEDIFF('DAY', " + getOperand1().sql() + ", " + getOperand2().sql() + ")";
	case MONTH:
	    return "DATEDIFF('MONTH', " + getOperand1().sql() + ", " + getOperand2().sql() + ")";
	case YEAR:
	    return "DATEDIFF('YEAR', " + getOperand1().sql() + ", " + getOperand2().sql() + ")";
	default:
	    throw new IllegalStateException("Unexpected interval unit: " + intervalUnit);
	}
    }

    private String sqlForMsSql() {
	switch (intervalUnit) {
	case HOUR:
	    return "DATEDIFF(HOUR, " + getOperand1().sql() + ", " + getOperand2().sql() + ")";
	case DAY:
	    return "DATEDIFF(DAY, " + getOperand1().sql() + ", " + getOperand2().sql() + ")";
	case MONTH:
	    return "DATEDIFF(MONTH, " + getOperand1().sql() + ", " + getOperand2().sql() + ")";
	case YEAR:
	    return "DATEDIFF(YEAR, " + getOperand1().sql() + ", " + getOperand2().sql() + ")";
	default:
	    throw new IllegalStateException("Unexpected interval unit: " + intervalUnit);
	}
    }

}