package ua.com.fielden.platform.eql.stage3.conditions;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.persistence.HibernateHelpers;
import ua.com.fielden.platform.utils.ToString;

import static ua.com.fielden.platform.entity.query.DbVersion.POSTGRESQL;
import static ua.com.fielden.platform.eql.dbschema.HibernateToJdbcSqlTypeCorrespondence.sqlCastTypeName;

public record ComparisonPredicate3 (ISingleOperand3 leftOperand,
                                    ComparisonOperator operator,
                                    ISingleOperand3 rightOperand)
        implements ICondition3, ToString.IFormattable
{

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        if (dbVersion == POSTGRESQL) {
            return String.format("%s %s %s",
                                 operandToSqlWithCast(leftOperand, rightOperand, metadata, dbVersion),
                                 operator,
                                 operandToSqlWithCast(rightOperand, leftOperand, metadata, dbVersion));
        } else {
            return String.format("%s %s %s", leftOperand.sql(metadata, dbVersion), operator,
                                 rightOperand.sql(metadata, dbVersion));
        }
    }

    private static String operandToSqlWithCast(final ISingleOperand3 operand, final ISingleOperand3 other,
                                               final IDomainMetadata metadata, final DbVersion dbVersion) {
        if (operand.type().isNull() && other.type().isNotNull()) {
            final var dialect = HibernateHelpers.getDialect(dbVersion);
            return dbVersion.castSql(operand.sql(metadata, dbVersion),
                                     sqlCastTypeName(other.type().hibType(), dialect));
        } else {
            return operand.sql(metadata, dbVersion);
        }
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines);
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("left", leftOperand)
                .add("right", rightOperand)
                .add("operator", operator)
                .$();
    }

}
