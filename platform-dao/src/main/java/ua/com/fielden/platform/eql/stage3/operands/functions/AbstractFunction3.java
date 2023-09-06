package ua.com.fielden.platform.eql.stage3.operands.functions;

import static java.lang.String.format;

import java.util.Date;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.exceptions.EqlStage3ProcessingException;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.AbstractSingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public abstract class AbstractFunction3 extends AbstractSingleOperand3 {
    
    public AbstractFunction3(PropType type) {
        super(type);
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        throw new EqlStage3ProcessingException(format("Function [%s] is not yet implemented for RDBMS [%s]!", getClass().getSimpleName(), dbVersion));
    }
    
    protected String getConvertToStringSql(final DbVersion dbVersion, final ISingleOperand3 operand) {
        switch (dbVersion) {
        case H2:
            return getConvertToStringSqlForH2(dbVersion, operand);
        case MSSQL:
            return getConvertToStringSqlForMsSql2005(dbVersion, operand);
        case POSTGRESQL:
            return getConvertToStringSqlForPostgresql(dbVersion, operand);
        case ORACLE:
            return getConvertToStringSqlForOracle(dbVersion, operand);
        default:
            throw new EqlStage3ProcessingException("Function of converting value to string [" + getClass().getSimpleName() + "] is not yet implemented for RDBMS [" + dbVersion + "]!");
        }
    }

    private String getConvertToStringSqlForOracle(final DbVersion dbVersion, final ISingleOperand3 operand) {
        if (operand.type() != null && Date.class.equals(operand.type().javaType())) {
            return "TO_CHAR(" + operand.sql(dbVersion) + ", 'YYYY-MM-dd hh24:mm:ss')";
        } else if (operand.type() != null && String.class.equals(operand.type().javaType())) {
            return operand.sql(dbVersion);
        } else {
            return "CAST(" + operand.sql(dbVersion) + " AS VARCHAR2(255))";
        }
    }

    private String getConvertToStringSqlForH2(final DbVersion dbVersion, final ISingleOperand3 operand) {
        if (operand.type() != null && Date.class.equals(operand.type().javaType())) {
            return "FORMATDATETIME(" + operand.sql(dbVersion) + ", 'YYYY-MM-dd hh:mm:ss')";
        } else if (operand.type() != null && String.class.equals(operand.type().javaType())) {
            return operand.sql(dbVersion);
        } else {
            return "CAST(" + operand.sql(dbVersion) + " AS VARCHAR(255))";
        }
    }

    public static String getConvertToStringSqlForMsSql2005(final DbVersion dbVersion, final ISingleOperand3 operand) {
        if (operand.type() != null && Date.class.equals(operand.type().javaType())) {
            return "CONVERT(VARCHAR(19), " + operand.sql(dbVersion) + ", 120)";
        } else if (operand.type() != null && String.class.equals(operand.type().javaType())) {
            return operand.sql(dbVersion);
        } else {
            return "CAST(" + operand.sql(dbVersion) + " AS VARCHAR(255))";
        }
    }

    public static String getConvertToStringSqlForPostgresql(final DbVersion dbVersion, final ISingleOperand3 operand) {
        if (operand.type() != null && Date.class.equals(operand.type().javaType())) {
            // TODO The date/time format should be read from IDates, once this contract is extended to support domain-specific data formats.
            final var opSql = operand.sql(dbVersion);
            final var expression = "case " +
                                   "when extract(milliseconds from %s) = 0 then to_char(%s, 'DD/MM/YYYY HH24:MI') " +
                                   "when cast(floor(extract(milliseconds from %s)) as integer) - 1000 * cast(floor(extract(seconds from %s)) as integer) > 0 then to_char(%s, 'DD/MM/YYYY HH24:MI:SS.MS') " +
                                   "else to_char(%s, 'DD/MM/YYYY HH24:MI:SS') end";
            return expression.formatted(opSql, opSql, opSql, opSql, opSql, opSql);
        } else if (operand.type() != null && String.class.equals(operand.type().javaType())) {
            return operand.sql(dbVersion);
        } else {
            return "CAST(" + operand.sql(dbVersion) + " AS VARCHAR(255))";
        }
    }
}
