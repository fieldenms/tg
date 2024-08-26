package ua.com.fielden.platform.eql.stage3.operands.functions;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit;
import ua.com.fielden.platform.eql.exceptions.EqlStage3ProcessingException;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;

import java.util.Objects;

import static java.lang.String.format;

public class CountDateInterval3 extends TwoOperandsFunction3 {

    private DateIntervalUnit intervalUnit;

    public CountDateInterval3(final DateIntervalUnit intervalUnit, final ISingleOperand3 periodEndDate, final ISingleOperand3 periodStartDate, final PropType type) {
        super(periodEndDate, periodStartDate, type);
        this.intervalUnit = intervalUnit;
    }

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        final String op1Sql = operand1.sql(metadata, dbVersion);
        final String op2Sql = operand2.sql(metadata, dbVersion);
        switch (dbVersion) {
        case H2:
            return sqlForH2(op1Sql, op2Sql);
        case MSSQL:
            return sqlForMsSql(op1Sql, op2Sql);
        case POSTGRESQL:
            return sqlForPostgres(op1Sql, op2Sql);
        default:
            return super.sql(metadata, dbVersion);
        }
    }

    private String sqlForPostgres(final String op1Sql, final String op2Sql) {
        final String yearSql = sqlForPostgresExtractYear(op1Sql, op2Sql);
        final String monthSql = sqlForPostgresExtractMonth(op1Sql, op2Sql);
        final String daySql = sqlForPostgresExtractDay(op1Sql, op2Sql);
        final String hourSql = sqlForPostgresExtractHour(op1Sql, op2Sql);
        final String minuteSql = sqlForPostgresExtractMinute(op1Sql, op2Sql);
        final String secondSql = sqlForPostgresExtractSecond(op1Sql, op2Sql);
        switch (intervalUnit) {
        case SECOND:
            return format("24 * 60 * 60 * (%s) + 60 * 60 * (%s) + 60 * (%s) + %s", daySql, hourSql, minuteSql, secondSql);
        case MINUTE:
            return format("24 * 60 * (%s)  + 60 * (%s) + %s", daySql, hourSql, minuteSql);
        case HOUR:
            return format("24 * (%s) + %s", daySql, hourSql);
        case DAY:
            return daySql;
        case MONTH:
            return format("12 * (%s) + %s", yearSql, monthSql);
        case YEAR:
            return yearSql;
        default:
            throw new EqlStage3ProcessingException("Unexpected interval unit: " + intervalUnit);
        }
    }

    private String sqlForPostgresExtractYear(final String operand1Sql, final String operand2Sql) {
        return format("EXTRACT(YEAR FROM (%s \\:\\:timestamp)) - EXTRACT(YEAR FROM (%s \\:\\:timestamp))", operand1Sql, operand2Sql);
    }

    private String sqlForPostgresExtractMonth(final String operand1Sql, final String operand2Sql) {
        return format("EXTRACT(MONTH FROM (%s \\:\\:timestamp)) - EXTRACT(MONTH FROM (%s \\:\\:timestamp))", operand1Sql, operand2Sql);
    }

    private String sqlForPostgresExtractDay(final String operand1Sql, final String operand2Sql) {
        return format("EXTRACT(DAY FROM (DATE_TRUNC('day', %s \\:\\:timestamp) - DATE_TRUNC('day', %s \\:\\:timestamp)))", operand1Sql, operand2Sql);
    }

    private String sqlForPostgresExtractHour(final String operand1Sql, final String operand2Sql) {
        return format("EXTRACT(HOUR FROM (%s \\:\\:timestamp)) - EXTRACT(HOUR FROM (%s \\:\\:timestamp))", operand1Sql, operand2Sql);
    }

    private String sqlForPostgresExtractMinute(final String operand1Sql, final String operand2Sql) {
        return format("EXTRACT(MINUTE FROM (%s \\:\\:timestamp)) - EXTRACT(MINUTE FROM (%s \\:\\:timestamp))", operand1Sql, operand2Sql);
    }

    private String sqlForPostgresExtractSecond(final String operand1Sql, final String operand2Sql) {
        return format("EXTRACT(SECOND FROM (%s \\:\\:timestamp)) - EXTRACT(SECOND FROM (%s \\:\\:timestamp))", operand1Sql, operand2Sql);
    }

    private String sqlForMssqlAndH2IntervalUnit(final String intervalUnitStr, final String op1Sql, final String op2Sql) {
        return format("DATEDIFF(%s, %s, %s)", intervalUnitStr, op2Sql, op1Sql);
    }
    
    private String sqlForH2(final String op1Sql, final String op2Sql) {
        switch (intervalUnit) {
        case SECOND:
            return sqlForMssqlAndH2IntervalUnit("'SECOND'", op1Sql, op2Sql);
        case MINUTE:
            return sqlForMssqlAndH2IntervalUnit("'MINUTE'", op1Sql, op2Sql);
        case HOUR:
            return sqlForMssqlAndH2IntervalUnit("'HOUR'", op1Sql, op2Sql);
        case DAY:
            return sqlForMssqlAndH2IntervalUnit("'DAY'", op1Sql, op2Sql);
        case MONTH:
            return sqlForMssqlAndH2IntervalUnit("'MONTH'", op1Sql, op2Sql);
        case YEAR:
            return sqlForMssqlAndH2IntervalUnit("'YEAR'", op1Sql, op2Sql);
        default:
            throw new EqlStage3ProcessingException("Unexpected interval unit: " + intervalUnit);
        }
    }

    private String sqlForMsSql(final String op1Sql, final String op2Sql) {
        switch (intervalUnit) {
        case SECOND:
            return sqlForMssqlAndH2IntervalUnit("SECOND", op1Sql, op2Sql);
        case MINUTE:
            return sqlForMssqlAndH2IntervalUnit("MINUTE", op1Sql, op2Sql);
        case HOUR:
            return sqlForMssqlAndH2IntervalUnit("HOUR", op1Sql, op2Sql);
        case DAY:
            return sqlForMssqlAndH2IntervalUnit("DAY", op1Sql, op2Sql);
        case MONTH:
            return sqlForMssqlAndH2IntervalUnit("MONTH", op1Sql, op2Sql);
        case YEAR:
            return sqlForMssqlAndH2IntervalUnit("YEAR", op1Sql, op2Sql);
        default:
            throw new EqlStage3ProcessingException("Unexpected interval unit: " + intervalUnit);
        }
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + intervalUnit.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
               || obj instanceof CountDateInterval3 that
                  && Objects.equals(intervalUnit, that.intervalUnit)
                  && super.equals(that);
    }
}
