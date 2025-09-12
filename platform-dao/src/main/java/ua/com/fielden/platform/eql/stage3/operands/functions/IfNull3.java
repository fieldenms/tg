package ua.com.fielden.platform.eql.stage3.operands.functions;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.queries.SubQuery3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.persistence.HibernateHelpers;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.query.DbVersion.POSTGRESQL;
import static ua.com.fielden.platform.eql.dbschema.HibernateToJdbcSqlTypeCorrespondence.sqlCastTypeName;

public class IfNull3 extends TwoOperandsFunction3 {

    public IfNull3(final ISingleOperand3 operand1, final ISingleOperand3 operand2, final PropType type) {
        super(operand1, operand2, type);
    }

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        final String operand1Sql;
        final String operand2Sql;
        // We need to help PostgreSQL with type inference by inserting explicit casts iff one of the operands is `null`.
        // See Issue #2213.
        if (dbVersion == POSTGRESQL && (operand1.type().isNull() ^ operand2.type().isNull())) {
            final var resultType = operand1.type().isNull() ? operand2.type() : operand1.type();
            final var dialect = HibernateHelpers.getDialect(dbVersion);
            operand1Sql = POSTGRESQL.castSql(operand1.sql(metadata, dbVersion), sqlCastTypeName(resultType.hibType(), dialect));
            operand2Sql = POSTGRESQL.castSql(operand2.sql(metadata, dbVersion), sqlCastTypeName(resultType.hibType(), dialect));
        }
        else {
            operand1Sql = operand1.sql(metadata, dbVersion);
            operand2Sql = operand2.sql(metadata, dbVersion);
        }

        if (operand1 instanceof SubQuery3) {
            // This optimisation ensures that the first argument to COALESCE is computed only once.
            // See Issue #2394 for more details.
            // The aliases are chosen to be unique enough.
            // The only place where a name conflict could occur is in the second argument to COALESCE,
            // if that expression happens to use one of the chosen aliases.
            // Although it is unlikely for a conflict to occur, a stronger guarantee would be to use a generated id,
            // such as TransformationContextFromStage2To3.sqlId.
            // However, that would require changing method IGenerateSql.sql to introduce another parameter,
            // which would demand significant refactoring effort.
            final String QUERY_ALIAS ="EQL_Q12778210642", COLUMN_ALIAS = "EQL_C51037967375";
            return "(SELECT COALESCE(%1$s.%2$s, %3$s) FROM (SELECT (%4$s) AS %2$s) AS %1$s)"
                   .formatted(QUERY_ALIAS, COLUMN_ALIAS, operand2Sql, operand1Sql);
        }
        else {
            return "COALESCE(%s, %s)".formatted(operand1Sql, operand2Sql);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + IfNull3.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof IfNull3;
    }

}
