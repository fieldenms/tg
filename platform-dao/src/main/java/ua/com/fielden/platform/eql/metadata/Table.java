package ua.com.fielden.platform.eql.metadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class Table {
    private final String name;
    private final SortedMap<String, Column> columns = new TreeMap<>();
    private final List<Column> columnsInPositionalOrder = new ArrayList<>();

    public Table(String name, List<ColumnData> columnData) {
        this.name = name;
        int ordinalPosition = 0;
        for (ColumnData col : columnData) {
            ordinalPosition = ordinalPosition + 1;
            Column column = new Column(this, col.name, col.dataType, "", col.columnSize, col.decimalDigits, col.nullable(), "", ordinalPosition, col.isNullable());
            columns.put(column.getName(), column);
            columnsInPositionalOrder.add(column);
        }
    }

    public String generateSchema() {
        StringBuffer sb = new StringBuffer();
        
        sb.append("CREATE TABLE ");
        sb.append(name);
        sb.append(" (");
        for (Iterator<Column> iterator = columnsInPositionalOrder.iterator(); iterator.hasNext();) {
            sb.append("\n");
            sb.append(iterator.next().generateSchema());
            if (iterator.hasNext()) {
                sb.append(",");    
            }
        }
        sb.append(")\n");
        return sb.toString();
    }
    
    
    public static class ColumnData {
        private final String name;
        private final boolean nullable;
        private final int dataType; // SQL type from java.sql.Types
        private final int columnSize; // column size. For char or date types this is the maximum number of characters, for numeric or decimal types this is precision.
        private final int decimalDigits; // the number of fractional digits

        public ColumnData(String name, boolean nullable, int dataType, int columnSize, int decimalDigits) {
            super();
            this.name = name;
            this.nullable = nullable;
            this.dataType = dataType;
            this.columnSize = columnSize;
            this.decimalDigits = decimalDigits;
        }

        public String isNullable() {
            return nullable ? "YES" : "NO";
        }

        public int nullable() {
            return nullable ? 1 : 0;
        }
    }
}