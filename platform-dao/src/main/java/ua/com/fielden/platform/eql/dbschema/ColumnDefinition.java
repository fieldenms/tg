package ua.com.fielden.platform.eql.dbschema;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.dialect.Dialect;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.dbschema.exceptions.DbSchemaException;

import java.sql.Types;
import java.util.Optional;

import static ua.com.fielden.platform.entity.query.DbVersion.POSTGRESQL;

/**
 * A data structure to capture the information required to generate column DDL statement.
 *
 * @param javaType could be useful for determining if the FK constraint is applicable
 */
public record ColumnDefinition(boolean unique,
                               Optional<Integer> compositeKeyMemberOrder,
                               boolean nullable,
                               String name,
                               Class<?> javaType,
                               int sqlType,
                               int length,
                               int scale,
                               int precision,
                               String defaultValue,
                               boolean requiresIndex)
{
    public static final int DEFAULT_STRING_LENGTH = 255;
    public static final int DEFAULT_NUMERIC_PRECISION = 18;
    public static final int DEFAULT_NUMERIC_SCALE = 2;

    private static final Logger LOGGER = LogManager.getLogger();
    public static final String WARN_NVARCHAR_NOT_SUPPORTED_POSTGRESQL =
    """
    NVARCHAR is not supported by PostgreSQL. Using VARCHAR instead for column [%s]. \
    The database must have a multi-byte encoding (e.g., UTF-8) for this to work as expected.""";

    public ColumnDefinition(
            final boolean unique,
            final Optional<Integer> compositeKeyMemberOrder,
            final boolean nullable,
            final String name,
            final Class<?> javaType,
            final int sqlType,
            final int length,
            final int scale,
            final int precision,
            final String defaultValue,
            final boolean requiresIndex)
    {
        if (StringUtils.isEmpty(name)) {
            throw new DbSchemaException("Column name can not be empty!");
        }
        this.unique = unique;
        this.compositeKeyMemberOrder = compositeKeyMemberOrder;
        this.nullable = nullable;
        this.name = name;
        this.javaType = javaType;
        this.sqlType = sqlType;
        this.length = length <= 0 ? DEFAULT_STRING_LENGTH : length;
        this.scale = scale <= -1 ? DEFAULT_NUMERIC_SCALE : scale;
        this.precision = precision <= -1 ? DEFAULT_NUMERIC_PRECISION : precision;
        this.defaultValue = defaultValue;
        this.requiresIndex = requiresIndex;
    }

    /**
     * Generates a DDL statement for a column based on provided RDBMS dialect.
     *
     * @param dialect
     * @return
     */
    public String schemaString(final Dialect dialect) {
        final StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(" ");

        sb.append(sqlTypeName(dialect));

        if (!nullable) {
            sb.append(" NOT NULL");
        }

        if (!defaultValue.isEmpty()) {
            sb.append(" DEFAULT ");
            sb.append(defaultValue);
        }

        return sb.toString();
    }

    /**
     * Converts a number that represents an SQL type to a human-readable descriptive text.
     * For example, MSSQL type {@code 12} becomes {@code "varchar(max)"}.
     */
    public String sqlTypeName(final Dialect dialect) {
        if (length == Integer.MAX_VALUE && String.class == javaType) {
            return switch (dbVersion(dialect)) {
                case POSTGRESQL -> "text";
                case MSSQL -> {
                    if (sqlType == Types.NVARCHAR) yield "nvarchar(max)";
                    else yield "varchar(max)";
                }
                default -> dialect.getTypeName(sqlType, length, precision, scale);
            };
        }
        else {
            if (sqlType == Types.NVARCHAR && dbVersion(dialect) == POSTGRESQL) {
                // Alternatively, "text" could be used, but it would disregard the length constraint.
                LOGGER.warn(WARN_NVARCHAR_NOT_SUPPORTED_POSTGRESQL.formatted(name));
                return dialect.getTypeName(Types.VARCHAR, length, precision, scale);
            }
            else {
                return dialect.getTypeName(sqlType, length, precision, scale);
            }
        }
    }

    private static DbVersion dbVersion(final Dialect dialect) {
        if (dialect.getClass().getSimpleName().startsWith("Postgre")) {
            return POSTGRESQL;
        }
        else if (dialect.getClass().getSimpleName().startsWith("SQLServer")) {
            return DbVersion.MSSQL;
        }
        throw new DbSchemaException("Unrecognised Hibernate dialect: %s".formatted(dialect));
    }

    @Override
    public int hashCode() {
        return 31 * name.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ColumnDefinition)) {
            return false;
        }
        final ColumnDefinition other = (ColumnDefinition) obj;

        return StringUtils.equals(name, other.name);
    }

}
