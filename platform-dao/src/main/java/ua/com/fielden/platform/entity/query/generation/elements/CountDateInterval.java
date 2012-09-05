package ua.com.fielden.platform.entity.query.generation.elements;

import ua.com.fielden.platform.entity.query.fluent.DateIntervalUnit;
import ua.com.fielden.platform.entity.query.generation.DbVersion;


public class CountDateInterval extends TwoOperandsFunction {

    private DateIntervalUnit intervalUnit;

    public CountDateInterval(final DateIntervalUnit intervalUnit, final ISingleOperand periodEndDate, final ISingleOperand periodStartDate, final DbVersion dbVersion) {
	super(dbVersion, periodEndDate, periodStartDate);
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
	case SECOND:
	    return "24 * 60 * 60 * " + sqlForPostgresExtractDay() + " + 60 * 60 * " + sqlForPostgresExtractHour() + " + 60 * " + sqlForPostgresExtractMinute() + " + " + sqlForPostgresExtractSecond();
	case MINUTE:
	    return "24 * 60 * " + sqlForPostgresExtractDay() + " + 60 * " + sqlForPostgresExtractHour() + " + " + sqlForPostgresExtractMinute();
	case HOUR:
	    return "24 * " + sqlForPostgresExtractDay() + " + " + sqlForPostgresExtractHour();
	case DAY:
	    return sqlForPostgresExtractDay();
//	case MONTH:
//	    return "EXTRACT(MONTH FROM (" + getOperand1().sql() + " - " + getOperand2().sql() + "))";
//	case YEAR:
//	    return "EXTRACT(YEAR FROM (" + getOperand1().sql() + " - " + getOperand2().sql() + "))";
	default:
	    throw new IllegalStateException("Unexpected interval unit: " + intervalUnit);
	}
    }

    private String sqlForPostgresExtractDay() {
	    return "EXTRACT(DAY FROM (" + getOperand1().sql() + " - " + getOperand2().sql() + "))";
    }

    private String sqlForPostgresExtractHour() {
	    return "EXTRACT(HOUR FROM (" + getOperand1().sql() + " - " + getOperand2().sql() + "))";
    }

    private String sqlForPostgresExtractMinute() {
	    return "EXTRACT(MINUTE FROM (" + getOperand1().sql() + " - " + getOperand2().sql() + "))";
    }

    private String sqlForPostgresExtractSecond() {
	    return "EXTRACT(SECOND FROM (" + getOperand1().sql() + " - " + getOperand2().sql() + "))";
    }

    private String sqlForH2() {
	switch (intervalUnit) {
	case SECOND:
	    return "DATEDIFF('SECOND', " + getOperand1().sql() + ", " + getOperand2().sql() + ")";
	case MINUTE:
	    return "DATEDIFF('MINUTE', " + getOperand1().sql() + ", " + getOperand2().sql() + ")";
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
	case SECOND:
	    return "DATEDIFF(SECOND, " + getOperand2().sql() + ", " + getOperand1().sql() + ")";
	case MINUTE:
	    return "DATEDIFF(MINUTE, " + getOperand2().sql() + ", " + getOperand1().sql() + ")";
	case HOUR:
	    return "DATEDIFF(HOUR, " + getOperand2().sql() + ", " + getOperand1().sql() + ")";
	case DAY:
	    return "DATEDIFF(DAY, " + getOperand2().sql() + ", " + getOperand1().sql() + ")";
	case MONTH:
	    return "DATEDIFF(MONTH, " + getOperand2().sql() + ", " + getOperand1().sql() + ")";
	case YEAR:
	    return "DATEDIFF(YEAR, " + getOperand2().sql() + ", " + getOperand1().sql() + ")";
	default:
	    throw new IllegalStateException("Unexpected interval unit: " + intervalUnit);
	}
    }
}