package ua.com.fielden.platform.eql.dbschema;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.dialect.Dialect;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.dbschema.exceptions.DbSchemaException;

import java.sql.Types;
import java.util.Optional;

import static ua.com.fielden.platform.entity.query.DbVersion.*;

/// A data structure to capture the information required to generate column DDL statement.
///
public class ColumnDefinition {
    public static final int DEFAULT_STRING_LENGTH = 255;
    public static final int DEFAULT_NUMERIC_PRECISION = 18;
    public static final int DEFAULT_NUMERIC_SCALE = 2;

    private static final Logger LOGGER = LogManager.getLogger();
    public static final String
            WARN_NVARCHAR_NOT_SUPPORTED_POSTGRESQL =
                """
                NVARCHAR is not supported by PostgreSQL. Using VARCHAR instead for column [%s]. \
                The database must have a multi-byte encoding (e.g., UTF-8) for this to work as expected.
                """,
            WARN_NVARCHAR_SIZE = "The size of NVARCHAR column [%s] was changed from [%s] to [MAX]. SQL Server uses MAX for size above 4000.",
            ERR_EMPTY_COLUMN_NAME = "Column name cannot be empty!",
            ERR_UNRECOGNISED_HIBERNATE_DIALECT = "Unrecognised Hibernate dialect: %s.";

    public final boolean unique;
    public final Optional<Integer> compositeKeyMemberOrder;
    public final boolean nullable;
    public final String name;
    public final Class<?> javaType;
    public final int sqlType;
    public final String sqlTypeName;
    public final int length;
    public final int scale;
    public final int precision;
    public final String defaultValue;
    public final Optional<ColumnIndex> maybeIndex;
    public final boolean indexApplicable;

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
            final Optional<ColumnIndex> maybeIndex,
            final Dialect dialect)
    {
        if (StringUtils.isEmpty(name)) {
            throw new DbSchemaException(ERR_EMPTY_COLUMN_NAME);
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
        this.sqlTypeName = sqlTypeName(dialect);
        this.maybeIndex = maybeIndex;
        this.indexApplicable = switch (dbVersion(dialect)) {
            // Not all columns can be indexable.
            // Refer to https://learn.microsoft.com/en-us/sql/t-sql/statements/create-index-transact-sql for more details.
            case MSSQL -> switch (sqlType) {
                case Types.VARCHAR, Types.VARBINARY, Types.NVARCHAR -> length != Integer.MAX_VALUE && !sqlTypeName.toLowerCase().contains("max");
                default -> true;
            };
            default -> true;
        };
    }

    /// Generates a DDL statement for a column based on provided RDBMS dialect.
    ///
    /// @param ignoreRequiredness  if `true`, the requiredness constraint is ignored (`NOT NULL` is not included)
    ///
    public String schemaString(final Dialect dialect, final boolean ignoreRequiredness) {
        final StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(" ");

        sb.append(sqlTypeName);

        if (!ignoreRequiredness && !nullable) {
            sb.append(" NOT NULL");
        }

        if (!defaultValue.isEmpty()) {
            sb.append(" DEFAULT ");
            sb.append(defaultValue);
        }

        return sb.toString();
    }

    /// Generates a DDL statement for a column based on provided RDBMS dialect.
    ///
    public String schemaString(final Dialect dialect) {
        return schemaString(dialect, false);
    }

    /// Converts a number that represents an SQL type to a human-readable descriptive text.
    /// For example, MSSQL type `12` becomes `"varchar(max)"`.
    ///
    private String sqlTypeName(final Dialect dialect) {
        if (length == Integer.MAX_VALUE && String.class == javaType) {
            return switch (dbVersion(dialect)) {
                case POSTGRESQL -> "text";
                case MSSQL -> sqlType == Types.NVARCHAR ?  "nvarchar(max)" : "varchar(max)";
                default -> dialect.getTypeName(sqlType, length, precision, scale);
            };
        }
        else if (sqlType == Types.NVARCHAR) {
            return switch(dbVersion(dialect)) {
                case POSTGRESQL -> {
                    // Alternatively, "text" could be used, but it would disregard the length constraint.
                    LOGGER.warn(() -> WARN_NVARCHAR_NOT_SUPPORTED_POSTGRESQL.formatted(name));
                    yield dialect.getTypeName(Types.VARCHAR, length, precision, scale);
                }
                case MSSQL -> {
                    final var typeName = dialect.getTypeName(sqlType, length, precision, scale);
                    if (typeName.toLowerCase().contains("max")) {
                        LOGGER.warn(() -> WARN_NVARCHAR_SIZE.formatted(name, length));
                    }
                    yield typeName;
                }
                default -> dialect.getTypeName(sqlType, length, precision, scale);
            };
        }
        else {
            return dialect.getTypeName(sqlType, length, precision, scale);
        }
    }

    private static DbVersion dbVersion(final Dialect dialect) {
        if (dialect.getClass().getSimpleName().startsWith("Postgre")) {
            return POSTGRESQL;
        }
        else if (dialect.getClass().getSimpleName().startsWith("SQLServer")) {
            return MSSQL;
        }
        else if (dialect.getClass().getSimpleName().startsWith("H2Dialect")) {
            return H2;
        }
        throw new DbSchemaException(ERR_UNRECOGNISED_HIBERNATE_DIALECT.formatted(dialect));
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
