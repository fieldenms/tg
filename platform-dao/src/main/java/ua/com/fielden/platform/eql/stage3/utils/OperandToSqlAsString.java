package ua.com.fielden.platform.eql.stage3.utils;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.exceptions.EqlStage3ProcessingException;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;

import java.util.Date;

/**
 * Utilities for transforming EQL operands into SQL expressions of a string type.
 */
public final class OperandToSqlAsString {

    /**
     * Generates an SQL expression from an operand. The expression is guaranteed to be of a string type.
     */
    public static String operandToSqlAsString(final IDomainMetadata metadata, final DbVersion dbVersion, final ISingleOperand3 operand) {
        final IToStringConvertor converter = switch (dbVersion) {
            case H2 -> ToStringConvertor.H2;
            case MSSQL -> ToStringConvertor.MSSQL;
            case POSTGRESQL -> ToStringConvertor.POSTGRESQL;
            case ORACLE -> ToStringConvertor.ORACLE;
            default -> throw new EqlStage3ProcessingException("Operand-to-string conversion is not implemented for RDBMS [%s]".formatted(dbVersion));
        };

        return converter.convert(metadata, operand);
    }

    /**
     * A contract for converting an operand to an SQL expression of a string type.
     */
    private interface IToStringConvertor {

        /**
         * Returns the DB version supported by this implementation.
         */
        DbVersion dbVersion();

        default String convert(final IDomainMetadata metadata, final ISingleOperand3 operand) {
            if (operand.type().javaType() == String.class) {
                return operand.sql(metadata, dbVersion());
            } else if (operand.type().javaType() == Integer.class) {
                return fromInteger(metadata, operand);
            } else if (operand.type().javaType() == Date.class) {
                return fromDate(metadata, operand);
            } else {
                return fromAny(metadata, operand);
            }
        }

        default String fromInteger(final IDomainMetadata metadata, final ISingleOperand3 operand) {
            // optimisation for integers: max integer value length is 10 chars
            return dbVersion().castSql(operand.sql(metadata, dbVersion()), "VARCHAR(10)");
        }

        default String fromDate(final IDomainMetadata metadata, final ISingleOperand3 operand) {
            return fromAny(metadata, operand);
        }

        default String fromAny(final IDomainMetadata metadata, final ISingleOperand3 operand) {
            return dbVersion().castSql(operand.sql(metadata, dbVersion()), "VARCHAR");
        }

    }

    private enum ToStringConvertor implements IToStringConvertor {
        H2 {
            @Override
            public DbVersion dbVersion() {
                return DbVersion.H2;
            }

            @Override
            public String fromDate(final IDomainMetadata metadata, final ISingleOperand3 operand) {
                return "FORMATDATETIME(" + operand.sql(metadata, dbVersion()) + ", 'YYYY-MM-dd hh:mm:ss')";
            }
        },

        MSSQL {
            @Override
            public DbVersion dbVersion() {
                return DbVersion.MSSQL;
            }

            @Override
            public String fromDate(final IDomainMetadata metadata, final ISingleOperand3 operand) {
                // TODO The date/time format should be read from IDates, once this contract is extended to support domain-specific data formats.
                //      However, there needs to be a database-specific translation between formats.
                final var opSql = operand.sql(metadata, dbVersion());
                final var expression = "case " +
                        // TODO dd/MM/yyyy should really only be used for the case of @DateOnly or LocalDate once supported
                        "when DATEPART(hour, %s) = 0 and DATEPART(minute, %s) = 0 and DATEPART(second, %s) = 0 and DATEPART(millisecond, %s) = 0 then FORMAT(%s, 'dd/MM/yyyy') " +
                        "when DATEPART(second, %s) = 0 and DATEPART(millisecond, %s) = 0 then FORMAT(%s, 'dd/MM/yyyy HH:mm') " +
                        "when DATEPART(millisecond, %s) > 0 then FORMAT(%s, 'dd/MM/yyyy HH:mm:ss.fff') " +
                        "else FORMAT(%s, 'dd/MM/yyyy HH:mm:ss') end";
                return expression.formatted(
                        opSql, opSql, opSql, opSql, opSql,
                        opSql, opSql, opSql,
                        opSql, opSql,
                        opSql);
            }
        },

        POSTGRESQL {
            @Override
            public DbVersion dbVersion() {
                return DbVersion.POSTGRESQL;
            }

            @Override
            public String fromDate(final IDomainMetadata metadata, final ISingleOperand3 operand) {
                // TODO The date/time format should be read from IDates, once this contract is extended to support domain-specific data formats.
                //      However, there needs to be a database-specific translation between formats.
                final var opSql = operand.sql(metadata, dbVersion());
                final var expression = "case " +
                        // TODO DD/MM/YYYY should really only be used for the case of @DateOnly or LocalDate once supported
                        "when extract(milliseconds from %s \\:\\:timestamp) = 0 and extract(minutes from %s \\:\\:timestamp) = 0 and extract(hours from %s \\:\\:timestamp) = 0 then to_char(%s \\:\\:timestamp , 'DD/MM/YYYY') " +
                        "when extract(milliseconds from %s \\:\\:timestamp) = 0 then to_char(%s \\:\\:timestamp, 'DD/MM/YYYY HH24:MI') " +
                        "when cast(floor(extract(milliseconds from %s \\:\\:timestamp)) as integer) - 1000 * cast(floor(extract(seconds from %s \\:\\:timestamp)) as integer) > 0 then to_char(%s \\:\\:timestamp, 'DD/MM/YYYY HH24:MI:SS.MS') " +
                        "else to_char(%s \\:\\:timestamp, 'DD/MM/YYYY HH24:MI:SS') end";
                return expression.formatted(
                        opSql, opSql, opSql, opSql,
                        opSql, opSql,
                        opSql, opSql, opSql,
                        opSql);
            }
        },

        ORACLE {
            @Override
            public DbVersion dbVersion() {
                return DbVersion.ORACLE;
            }

            @Override
            public String fromDate(final IDomainMetadata metadata, final ISingleOperand3 operand) {
                return "TO_CHAR(" + operand.sql(metadata, dbVersion()) + ", 'YYYY-MM-dd hh24:mm:ss')";
            }

            @Override
            public String fromAny(final IDomainMetadata metadata, final ISingleOperand3 operand) {
                return DbVersion.ORACLE.castSql(operand.sql(metadata, dbVersion()), "VARCHAR2");
            }
        }
    }

}
