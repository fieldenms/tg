package ua.com.fielden.platform.eql.meta;

import static java.util.Collections.unmodifiableSortedMap;

import java.util.SortedMap;

/**
 * An abstraction for representing a DB table, used to store an entity.
 *
 * @author TG Team
 *
 */
public class Table {
    public final String name;
    public final SortedMap<String, PropColumnInfo> columns;

    public Table(final String name, final SortedMap<String, PropColumnInfo> columns) {
        this.name = name;
        this.columns = unmodifiableSortedMap(columns);
    }
    
    public static class PropColumnInfo {
        public final String columnName;
        public final Class<?> type;
        public final Object hibType;
        
        public PropColumnInfo(final String columnName, final Class<?> type, final Object hibType) {
            this.columnName = columnName;
            this.type = type;
            this.hibType = hibType;
        }
    }
}