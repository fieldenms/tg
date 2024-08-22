package ua.com.fielden.platform.eql.stage3.conditions;

import ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.persistence.HibernateHelpers;

import java.util.Objects;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.query.DbVersion.POSTGRESQL;
import static ua.com.fielden.platform.eql.dbschema.HibernateToJdbcSqlTypeCorrespondence.sqlCastTypeName;

public class ComparisonPredicate3 implements ICondition3 {
    public final ISingleOperand3 leftOperand;
    public final ISingleOperand3 rightOperand;
    public final ComparisonOperator operator;

    public ComparisonPredicate3(final ISingleOperand3 leftOperand, final ComparisonOperator operator, final ISingleOperand3 rightOperand) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.operator = operator;
    }

    @Override
    public String sql(final IDomainMetadata metadata) {
        if (metadata.dbVersion() == POSTGRESQL) {
            return format("%s %s %s",
                    operandToSqlWithCast(leftOperand, rightOperand, metadata),
                    operator,
                    operandToSqlWithCast(rightOperand, leftOperand, metadata));
        } else {
            return format("%s %s %s", leftOperand.sql(metadata), operator, rightOperand.sql(metadata));
        }
    }

    private static String operandToSqlWithCast(final ISingleOperand3 operand, final ISingleOperand3 other, final IDomainMetadata metadata) {
        if (operand.type().isNull() && other.type().isNotNull()) {
            final var dialect = HibernateHelpers.getDialect(metadata.dbVersion());
            return metadata.dbVersion().castSql(operand.sql(metadata), sqlCastTypeName(other.type().hibType(), dialect));
        } else {
            return operand.sql(metadata);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + leftOperand.hashCode();
        result = prime * result + operator.hashCode();
        result = prime * result + rightOperand.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ComparisonPredicate3)) {
            return false;
        }

        final ComparisonPredicate3 other = (ComparisonPredicate3) obj;

        return Objects.equals(leftOperand, other.leftOperand) &&
                Objects.equals(rightOperand, other.rightOperand) &&
                Objects.equals(operator, other.operator);
    }

}
