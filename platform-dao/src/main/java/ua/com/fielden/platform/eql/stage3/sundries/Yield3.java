package ua.com.fielden.platform.eql.stage3.sundries;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.persistence.HibernateHelpers;

import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.query.DbVersion.POSTGRESQL;
import static ua.com.fielden.platform.eql.dbschema.HibernateToJdbcSqlTypeCorrespondence.sqlCastTypeName;
import static ua.com.fielden.platform.eql.meta.PropType.propType;
import static ua.com.fielden.platform.persistence.types.PlaceholderType.newPlaceholderType;

public class Yield3 {

    public final ISingleOperand3 operand;
    public final String alias;

    /** Name of the column in the resulting set that this value is yielded under or {@code null}. */
    public final String column;

    /**
     * The expected type of this yield.
     * <ul>
     *   <li>for calculated properties -- the type declared at the model level; the type of {@link #operand} will be
     *       inferred from the actual expression and may be different.
     *   <li>for other operands -- equal to the type of {@link #operand}.
     * </ul>
     */
    public final PropType type;

    public Yield3(final ISingleOperand3 operand, final String alias, final int columnId, final PropType type) {
        this.operand = operand;
        this.alias = alias;
        this.column = isEmpty(alias) ? null : "C_" + columnId;
        this.type = type;
    }

    /**
     * @param expectedType  unless equal to {@link #NO_EXPECTED_TYPE}, then the yielded value is cast to the given type
     */
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion, final PropType expectedType) {
        final String operandSql = operand.sql(metadata, dbVersion);
        final var sb = new StringBuilder(operandSql.length());

        // Cast even if the expected type is the same as this type to cover the auto-yield case where a yielded null
        // can be represented as Prop3 "id" with type Long.
        // Expected type should never be the null type but let's be vigilant.
        if (dbVersion == POSTGRESQL && expectedType != NO_EXPECTED_TYPE && expectedType.isNotNull()) {
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

    /**
     * A placeholder value to be used when there is no expectation of a particular type.
     *
     * @see #sql(IDomainMetadata, DbVersion, PropType)
     *
     */
    public static final PropType NO_EXPECTED_TYPE = propType(String.class, newPlaceholderType("no_expected_type"));

    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return sql(metadata, dbVersion, NO_EXPECTED_TYPE);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((alias == null) ? 0 : alias.hashCode());
        result = prime * result + operand.hashCode();
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Yield3)) {
            return false;
        }
        
        final Yield3 other = (Yield3) obj;
        
        return Objects.equals(operand, other.operand) && Objects.equals(alias, other.alias) && Objects.equals(type, other.type);
    }

    @Override
    public String toString() {
        return "Yield3(alias=%s, column=%s, type=%s, operand=%s)".formatted(alias, column, type, operand);
    }

}
