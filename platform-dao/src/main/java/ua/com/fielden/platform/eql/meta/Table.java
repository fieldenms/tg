package ua.com.fielden.platform.eql.meta;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ua.com.fielden.platform.utils.CollectionUtil;

/**
 * An abstraction for representing a DB table, used to store an entity.
 *
 * @author TG Team
 *
 */
public class Table {
    public final String name;
    public final List<PropColumnInfo> columns;

    public Table(final String name, final List<PropColumnInfo> columns) {
        this.name = name;
        this.columns = unmodifiableList(columns);
    }
    
    // TODO convert to a record in Modern Java to ensure equals and hashCode.
    public static class PropColumnInfo {
        public final String leafPropName;
        public final Set<String> columnNames; // need to support plural columns because of CompositeUserType props.
        public final Object hibType;
        
        public PropColumnInfo(final String leafPropName, final String columnName, final Object hibType) {
            this.leafPropName = leafPropName;
            this.columnNames =  CollectionUtil.unmodifiableSetOf(columnName);
            this.hibType = hibType;
        }

        public PropColumnInfo(final String leafPropName, final List<String> columnNames, final Object hibType) {
            this.leafPropName = leafPropName;
            this.columnNames = unmodifiableSet(new LinkedHashSet<>(columnNames));
            this.hibType = hibType;
        }
    }

}