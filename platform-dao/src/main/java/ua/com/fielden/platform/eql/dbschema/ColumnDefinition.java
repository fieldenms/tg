package ua.com.fielden.platform.eql.dbschema;

import org.apache.commons.lang.StringUtils;
import org.hibernate.dialect.Dialect;

import ua.com.fielden.platform.eql.dbschema.exceptions.DbSchemaException;

/**
 * A data structure to capture the information required to generate column DDL statement. 
 * 
 * @author TG Team
 *
 */
public class ColumnDefinition {
    public final boolean nullable;
    public final String name;
    public final Class<?> javaType; // could be useful for determining if the FK constraint is applicable
    public final int sqlType;
    public final int length;
    public final int scale;
    public final int precision;
    public final String defaultValue;
    public final TableDefinition tableDefinition;
    
    public static final int DEFAULT_STRING_LENGTH = 255;
    public static final int DEFAULT_NUMERIC_PRECISION = 18;
    public static final int DEFAULT_NUMERIC_SCALE = 2;

    public ColumnDefinition(
            final TableDefinition tableDefinition,
            final boolean nullable, 
            final String name, 
            final Class<?> javaType, 
            final int sqlType, 
            final int length, 
            final int scale, 
            final int precision, 
            final String defaultValue) {
        if (StringUtils.isEmpty(name)) {
            throw new DbSchemaException("Column name can not be empty!");
        }
        this.tableDefinition = tableDefinition;
        this.nullable = nullable;
        this.name = name;
        this.javaType = javaType;
        this.sqlType = sqlType;
        this.length = length <= 0 ? DEFAULT_STRING_LENGTH : length;
        this.scale = scale <= -1 ? DEFAULT_NUMERIC_SCALE : scale;
        this.precision = precision <= -1 ? DEFAULT_NUMERIC_PRECISION : precision;
        this.defaultValue = defaultValue;
    }

    /**
     * Generates DDL statement for a column based on provided RDBMS dialect.
     * 
     * @param dialect
     * @return
     */
    public String schemaString(final Dialect dialect) {
        final StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(" ");
        sb.append(dialect.getTypeName(sqlType, length, precision, scale));
        
        if (!nullable) {
            sb.append(" NOT NULL");
        }

        if (!defaultValue.isEmpty()) {
            sb.append(" DEFAULT ");
            sb.append(defaultValue);
        }
        
        return sb.toString();
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