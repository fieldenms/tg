package ua.com.fielden.platform.eql.stage3.sundries;

import org.hibernate.type.Type;
import org.hibernate.usertype.UserType;
import ua.com.fielden.platform.eql.dbschema.HibernateToJdbcSqlTypeCorrespondence;
import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.query.DbVersion.POSTGRESQL;
import static ua.com.fielden.platform.eql.dbschema.HibernateToJdbcSqlTypeCorrespondence.genericSqlTypeName;

public class Yield3 {

    public final ISingleOperand3 operand;
    public final String alias;

    /** Name of the column in the resulting set that this value is yielded under or {@code null}. */
    public final String column;

    /**
     * The type declared at the model level.
     * <ul>
     *   <li>for non-calculated properties -- the same as the type of {@link #operand}
     *   <li>for calculated -- the type of {@link #operand} will be inferred from the actual expression and may differ
     *       from the declared one.
     *   <li>for other operands -- should be equal to the type of {@link #operand}
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
    public String sql(final EqlDomainMetadata metadata, final PropType expectedType) {
        final String operandSql = operand.sql(metadata);
        final var sb = new StringBuilder(operandSql.length());

        // cast even if expected type equals the declared type (crucial for auto-yields)
        if (metadata.dbVersion == POSTGRESQL && expectedType != null) {
            sb.append("CAST (");
            sb.append(operandSql);
            sb.append(" AS ");
            sb.append(sqlCastTypeName(expectedType, metadata));
            sb.append(')');
        }
        else {
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
     * @see #sql(EqlDomainMetadata, PropType)
     */
    public static final PropType NO_EXPECTED_TYPE = null; // deliberate null

    public String sql(final EqlDomainMetadata metadata) {
        return sql(metadata, NO_EXPECTED_TYPE);
    }

    private static String sqlCastTypeName(final PropType type, final EqlDomainMetadata metadata) {
        final int sqlType = switch (type.hibType()) {
            case Type t -> HibernateToJdbcSqlTypeCorrespondence.jdbcSqlTypeFor(t);
            case UserType t -> HibernateToJdbcSqlTypeCorrespondence.jdbcSqlTypeFor(t);
            default -> throw new IllegalArgumentException("Unexpected Hibernate type of yielded value: %s".formatted(type.hibType()));
        };

        return genericSqlTypeName(sqlType, metadata.dialect);
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
}
