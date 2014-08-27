package ua.com.fielden.platform.dao;

import java.util.SortedMap;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;

public class PersistedEntityMetadata<ET extends AbstractEntity<?>> extends AbstractEntityMetadata<ET>{
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((table == null) ? 0 : table.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof PersistedEntityMetadata))
            return false;
        PersistedEntityMetadata other = (PersistedEntityMetadata) obj;
        if (table == null) {
            if (other.table != null)
                return false;
        } else if (!table.equals(other.table))
            return false;
        return true;
    }

    private final String table;

    public PersistedEntityMetadata(final String table, final Class<ET> type, final SortedMap<String, PropertyMetadata> props) {
        super(type, props);
        this.table = table;
    }

    public String getTable() {
        return table;
    }
}