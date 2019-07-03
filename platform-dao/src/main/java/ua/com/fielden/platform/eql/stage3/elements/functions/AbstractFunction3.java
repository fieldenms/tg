package ua.com.fielden.platform.eql.stage3.elements.functions;

import static java.lang.String.format;

import java.util.Date;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public abstract class AbstractFunction3 implements ISingleOperand3 {
    
    @Override
    public String sql(final DbVersion dbVersion) {
        throw new EqlException(format("Function [%s] is not yet implemented for RDBMS [%s]!", getClass().getSimpleName(), dbVersion));
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
            throw new IllegalStateException("Function of converting value to string [" + getClass().getSimpleName() + "] is not yet implemented for RDBMS [" + dbVersion + "]!");
        }
    }

    private String getConvertToStringSqlForOracle(final DbVersion dbVersion, final ISingleOperand3 operand) {
        if (Date.class.equals(operand.type())) {
            return "TO_CHAR(" + operand.sql(dbVersion) + ", 'YYYY-MM-dd hh24:mm:ss')";
        } else if (String.class.equals(operand.type())) {
            return operand.sql(dbVersion);
        } else {
            return "CAST(" + operand.sql(dbVersion) + " AS VARCHAR2(255))";
        }
    }

    private String getConvertToStringSqlForH2(final DbVersion dbVersion, final ISingleOperand3 operand) {
        if (Date.class.equals(operand.type())) {
            return "FORMATDATETIME(" + operand.sql(dbVersion) + ", 'YYYY-MM-dd hh:mm:ss')";
        } else if (String.class.equals(operand.type())) {
            return operand.sql(dbVersion);
        } else {
            return "CAST(" + operand.sql(dbVersion) + " AS VARCHAR(255))";
        }
    }

    private String getConvertToStringSqlForMsSql2005(final DbVersion dbVersion, final ISingleOperand3 operand) {
        if (Date.class.equals(operand.type())) {
            return "CONVERT(VARCHAR(19), " + operand.sql(dbVersion) + ", 120)";
        } else if (String.class.equals(operand.type())) {
            return operand.sql(dbVersion);
        } else {
            return "CAST(" + operand.sql(dbVersion) + " AS VARCHAR(255))";
        }
    }

    private String getConvertToStringSqlForPostgresql(final DbVersion dbVersion, final ISingleOperand3 operand) {
        if (Date.class.equals(operand.type())) {
            return "TO_CHAR(" + operand.sql(dbVersion) + ", 'YYYY-MM-DD HH24:MI:SS')";
        } else if (String.class.equals(operand.type())) {
            return operand.sql(dbVersion);
        } else {
            return "CAST(" + operand.sql(dbVersion) + " AS VARCHAR(255))";
        }
    }
}
