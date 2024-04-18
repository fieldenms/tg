package ua.com.fielden.platform.eql.stage3.conditions;

import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.Value3;

import java.util.Date;
import java.util.Objects;

import static ua.com.fielden.platform.entity.query.DbVersion.POSTGRESQL;
import static ua.com.fielden.platform.eql.dbschema.HibernateToJdbcSqlTypeCorrespondence.sqlCastTypeName;
import static ua.com.fielden.platform.eql.meta.EqlEntityMetadataGenerator.H_DATETIME;

public class NullPredicate3 implements ICondition3 {
    public final ISingleOperand3 operand;
    private final boolean negated;

    public NullPredicate3(final ISingleOperand3 operand, final boolean negated) {
        this.operand = operand;
        this.negated = negated;
    }

    @Override
    public String sql(final EqlDomainMetadata metadata) {
        final String operandSql = operand.sql(metadata);

        final String exprSql;
        /* PostgreSQL can't always infer the type of date literals, requires an explicit cast.
         * Check for Date assignability to cover all possible values (e.g., java.sql.Timestamp). */
        if (metadata.dbVersion == POSTGRESQL && operand instanceof Value3 && operand.type().isNotNull() && Date.class.isAssignableFrom(operand.type().javaType())) {
            // hibType of operand is not reliable, may be null, so just use a standard datetime type which should suffice for the purpose of IS NULL
            exprSql = POSTGRESQL.castSql(operandSql, sqlCastTypeName(H_DATETIME, metadata.dialect));
        } else {
            exprSql = operandSql;
        }

        return exprSql + " IS " + (negated ? "NOT" : "") + " NULL";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (negated ? 1231 : 1237);
        result = prime * result + operand.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof NullPredicate3)) {
            return false;
        }
        final NullPredicate3 other = (NullPredicate3) obj;

        return (negated == other.negated) && Objects.equals(operand, other.operand);
    }

}
