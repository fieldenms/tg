package ua.com.fielden.platform.eql.stage3.sundries;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.persistence.HibernateHelpers;
import ua.com.fielden.platform.utils.ToString;

import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.query.DbVersion.POSTGRESQL;
import static ua.com.fielden.platform.eql.dbschema.HibernateToJdbcSqlTypeCorrespondence.sqlCastTypeName;
import static ua.com.fielden.platform.eql.meta.PropType.NULL_TYPE;

/**
 *
 * @param operand
 * @param column  name of the column in the result set that this operand is yielded under.
 * <p> Can also be {@code null}, but only in the following cases:
 * <ul>
 *   <li> When directly enclosed by an {@linkplain ua.com.fielden.platform.eql.stage3.queries.SubQueryForExists3 existence sub-query}.
 *   <li> When directly enclosed by a {@linkplain ua.com.fielden.platform.eql.stage3.queries.SubQuery3 sub-query}.
 * </ul>
 * @param type  the expected type of this yield:
 * <ul>
 *   <li>For calculated properties -- the type declared at the model level; the type of {@link #operand} will be
 *       inferred from the actual expression and may be different.
 *   <li>For other operands -- equal to the type of {@link #operand}.
 * </ul>
 */
public record Yield3 (ISingleOperand3 operand, String alias, String column, PropType type) implements ToString.IFormattable {

    public Yield3(final ISingleOperand3 operand, final String alias, final int columnId, final PropType type) {
        this(operand, alias, isEmpty(alias) ? null : "C_" + columnId, type);
    }

    /// @param expectedType  unless it is the [PropType#NULL_TYPE], the yielded value is cast to the given type
    ///
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion, final PropType expectedType) {
        final String operandSql = operand.sql(metadata, dbVersion);
        final var sb = new StringBuilder(operandSql.length());

        // Cast even if the expected type is the same as this type to cover the auto-yield case where a yielded null
        // can be represented as Prop3 "id" with type Long.
        if (dbVersion == POSTGRESQL && expectedType.isNotNull()) {
            final var dialect = HibernateHelpers.getDialect(dbVersion);
            sb.append(POSTGRESQL.castSql(operandSql, sqlCastTypeName(expectedType.hibType(), dialect)));
        } else {
            sb.append(operandSql);
        }

        if (column != null) {
            sb.append(" AS ");
            sb.append(column);
        }

        return sb.toString();
    }

    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return sql(metadata, dbVersion, NULL_TYPE);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alias, operand, type);
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
               || obj instanceof Yield3 that
                  && Objects.equals(operand, that.operand)
                  && Objects.equals(alias, that.alias)
                  && Objects.equals(type, that.type);
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines());
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("type", type)
                .addIfNotNull("alias", alias)
                .addIfNotNull("column", column)
                .add("operand", operand)
                .$();
    }

}
