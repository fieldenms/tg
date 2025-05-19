package ua.com.fielden.platform.eql.stage3.operands.functions;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.ToString;

import java.util.Objects;

import static java.lang.String.format;

public class CountDateInterval3 extends TwoOperandsFunction3 {

    private final DateIntervalUnit intervalUnit;

    public CountDateInterval3(final DateIntervalUnit intervalUnit, final ISingleOperand3 periodEndDate, final ISingleOperand3 periodStartDate, final PropType type) {
        super(periodEndDate, periodStartDate, type);
        this.intervalUnit = intervalUnit;
    }

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        final String op1Sql = operand1.sql(metadata, dbVersion);
        final String op2Sql = operand2.sql(metadata, dbVersion);
        return switch (dbVersion) {
            case H2 -> sqlForH2(op1Sql, op2Sql);
            case MSSQL -> sqlForMsSql(op1Sql, op2Sql);
            case POSTGRESQL -> sqlForPostgres(op1Sql, op2Sql);
            default -> super.sql(metadata, dbVersion);
        };
    }

    private String sqlForPostgres(final String op1Sql, final String op2Sql) {
        final String yearSql = sqlForPostgresExtractYear(op1Sql, op2Sql);
        final String monthSql = sqlForPostgresExtractMonth(op1Sql, op2Sql);
        final String daySql = sqlForPostgresExtractDay(op1Sql, op2Sql);
        final String hourSql = sqlForPostgresExtractHour(op1Sql, op2Sql);
        final String minuteSql = sqlForPostgresExtractMinute(op1Sql, op2Sql);
        final String secondSql = sqlForPostgresExtractSecond(op1Sql, op2Sql);
        return switch (intervalUnit) {
            case SECOND -> format("24 * 60 * 60 * (%s) + 60 * 60 * (%s) + 60 * (%s) + %s", daySql, hourSql, minuteSql, secondSql);
            case MINUTE -> format("24 * 60 * (%s) + 60 * (%s) + %s", daySql, hourSql, minuteSql);
            case HOUR -> format("24 * (%s) + %s", daySql, hourSql);
            case DAY -> daySql;
            case MONTH -> format("12 * (%s) + %s", yearSql, monthSql);
            case YEAR -> yearSql;
        };
    }

    private String sqlForH2(final String op1Sql, final String op2Sql) {
        return switch (intervalUnit) {
            case SECOND -> sqlForMssqlAndH2IntervalUnit("'SECOND'", op1Sql, op2Sql);
            case MINUTE -> sqlForMssqlAndH2IntervalUnit("'MINUTE'", op1Sql, op2Sql);
            case HOUR -> sqlForMssqlAndH2IntervalUnit("'HOUR'", op1Sql, op2Sql);
            case DAY -> sqlForMssqlAndH2IntervalUnit("'DAY'", op1Sql, op2Sql);
            case MONTH -> sqlForMssqlAndH2IntervalUnit("'MONTH'", op1Sql, op2Sql);
            case YEAR -> sqlForMssqlAndH2IntervalUnit("'YEAR'", op1Sql, op2Sql);
        };
    }

    private String sqlForMsSql(final String op1Sql, final String op2Sql) {
        return switch (intervalUnit) {
            case SECOND -> sqlForMssqlAndH2IntervalUnit("SECOND", op1Sql, op2Sql);
            case MINUTE -> sqlForMssqlAndH2IntervalUnit("MINUTE", op1Sql, op2Sql);
            case HOUR -> sqlForMssqlAndH2IntervalUnit("HOUR", op1Sql, op2Sql);
            case DAY -> sqlForMssqlAndH2IntervalUnit("DAY", op1Sql, op2Sql);
            case MONTH -> sqlForMssqlAndH2IntervalUnit("MONTH", op1Sql, op2Sql);
            case YEAR -> sqlForMssqlAndH2IntervalUnit("YEAR", op1Sql, op2Sql);
        };
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

    @Override
    protected ToString addToString(final ToString toString) {
        return super.addToString(toString).add("unit", intervalUnit);
    }

    private static String sqlForPostgresExtractYear(final String operand1Sql, final String operand2Sql) {
        return format("EXTRACT(YEAR FROM (%s \\:\\:timestamp)) - EXTRACT(YEAR FROM (%s \\:\\:timestamp))", operand1Sql, operand2Sql);
    }

    private static String sqlForPostgresExtractMonth(final String operand1Sql, final String operand2Sql) {
        return format("EXTRACT(MONTH FROM (%s \\:\\:timestamp)) - EXTRACT(MONTH FROM (%s \\:\\:timestamp))", operand1Sql, operand2Sql);
    }

    private static String sqlForPostgresExtractDay(final String operand1Sql, final String operand2Sql) {
        return format("EXTRACT(DAY FROM (DATE_TRUNC('day', %s \\:\\:timestamp) - DATE_TRUNC('day', %s \\:\\:timestamp)))", operand1Sql, operand2Sql);
    }

    private static String sqlForPostgresExtractHour(final String operand1Sql, final String operand2Sql) {
        return format("EXTRACT(HOUR FROM (%s \\:\\:timestamp)) - EXTRACT(HOUR FROM (%s \\:\\:timestamp))", operand1Sql, operand2Sql);
    }

    private static String sqlForPostgresExtractMinute(final String operand1Sql, final String operand2Sql) {
        return format("EXTRACT(MINUTE FROM (%s \\:\\:timestamp)) - EXTRACT(MINUTE FROM (%s \\:\\:timestamp))", operand1Sql, operand2Sql);
    }

    private static String sqlForPostgresExtractSecond(final String operand1Sql, final String operand2Sql) {
        return format("EXTRACT(SECOND FROM (%s \\:\\:timestamp)) - EXTRACT(SECOND FROM (%s \\:\\:timestamp))", operand1Sql, operand2Sql);
    }

    private static String sqlForMssqlAndH2IntervalUnit(final String intervalUnitStr, final String op1Sql, final String op2Sql) {
        return format("DATEDIFF(%s, %s, %s)", intervalUnitStr, op2Sql, op1Sql);
    }

}
