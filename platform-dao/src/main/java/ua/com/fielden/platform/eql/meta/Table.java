package ua.com.fielden.platform.eql.meta;

import java.util.SortedMap;

public class Table {
    public final String name;
    public final SortedMap<String, PropColumnInfo> columns;

    public Table(final String name, final SortedMap<String, PropColumnInfo> columns) {
        this.name = name;
        this.columns = columns;
    }
    
    public static class PropColumnInfo {
        public final String columnName;
        public final Class<?> type;
        public final Object hibType;
        
        public PropColumnInfo(String columnName, Class<?> type, Object hibType) {
            this.columnName = columnName;
            this.type = type;
            this.hibType = hibType;
        }
    }
}