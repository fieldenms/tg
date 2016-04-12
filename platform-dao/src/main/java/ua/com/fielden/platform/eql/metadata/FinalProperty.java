package ua.com.fielden.platform.eql.metadata;

import org.apache.commons.lang.StringUtils;

public class FinalProperty {
    private boolean nullable;
    private String name;
    private String columnNameSuggestion;
    private Class javaType;
    private int sqlType;
    private int length;
    private int scale;
    private int precision;
    private String defaultValue;

    public FinalProperty(boolean nullable, String name, String columnNameSuggestion, Class javaType, int sqlType, int length, int scale, int precision, String defaultValue) {
        super();
        this.nullable = nullable;
        this.name = name;
        this.columnNameSuggestion = columnNameSuggestion;
        this.javaType = javaType;
        this.sqlType = sqlType;
        this.length = length;
        this.scale = scale;
        this.precision = precision;
        this.defaultValue = defaultValue;
    }

    public String nameClause() {
        return (StringUtils.isNotBlank(columnNameSuggestion) ? columnNameSuggestion : name.toUpperCase() + "_");
    }

    public boolean isNullable() {
        return nullable;
    }

    public int getSqlType() {
        return sqlType;
    }

    public int getLength() {
        return length;
    }

    public int getScale() {
        return scale;
    }

    public int getPrecision() {
        return precision;
    }
}