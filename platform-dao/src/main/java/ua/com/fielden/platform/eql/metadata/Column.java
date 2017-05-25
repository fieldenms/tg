package ua.com.fielden.platform.eql.metadata;

public class Column {
    private final Table table;
    private final String name;
    private final int dataType; // SQL type from java.sql.Types
    private final String typeName; // Data source dependent type name, for a UDT the type name is fully qualified
    private final int columnSize; // column size. For char or date types this is the maximum number of characters, for numeric or decimal types this is precision.
    private final int decimalDigits; // the number of fractional digits
    private final int nullable; // is NULL allowed. columnNoNulls - might not allow NULL values, columnNullable - definitely allows NULL values, columnNullableUnknown - nullability unknown
    private final String columnDef;// default value (may be null)
    private final int ordinalPosition; // index of column in table (starting at 1)
    private final String isNullable; // "NO" means column definitely does not allow NULL values; "YES" means the column might allow NULL values. An empty string means nobody knows.

    public Column(Table table, String name, int dataType, String typeName, int columnSize, int decimalDigits, int nullable, String columnDef, int ordinalPosition, String isNullable) {
        super();
        this.table = table;
        this.name = name;
        this.dataType = dataType;
        this.typeName = typeName;
        this.columnSize = columnSize;
        this.decimalDigits = decimalDigits;
        this.nullable = nullable;
        this.columnDef = columnDef;
        this.ordinalPosition = ordinalPosition;
        this.isNullable = isNullable;
    }

    public String getName() {
        return name;
    }
    
    private String nullabilityClause() {
        return (nullable == 1) ? "NULL" : "NOT NULL";       
    }
    
    private String typeClause() {
        if (dataType == 12) {
            return "VARCHAR(" + columnSize + ")";
        } else if (dataType == 2) {
            return "NUMERIC(" + columnSize + ", " + decimalDigits + ")";
        } else if (dataType == 4) {
            return "INT";
        } else if (dataType == -5) {
            return "BIGINT";
        } else if (dataType == 93) {
            return "DATETIME";
        } else if (dataType == -7) {
            return "CHAR(1)";
        } else {
            return "?-" + dataType;
        }
    }
    
    public String generateSchema() {
        StringBuffer sb = new StringBuffer();
        sb.append(name);
        sb.append(" ");
        sb.append(typeClause());
        sb.append(" ");
        sb.append(nullabilityClause());
        
        return sb.toString();
    }
}