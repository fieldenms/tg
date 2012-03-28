package ua.com.fielden.platform.dao2;

import java.util.SortedMap;

import ua.com.fielden.platform.entity.AbstractEntity;

public class EntityPersistenceMetadata {
    private final String table;
    private final Class<? extends AbstractEntity<?>> type;
    private final SortedMap<String, PropertyPersistenceInfo> props;

    public EntityPersistenceMetadata(final String table, final Class<? extends AbstractEntity<?>> type, final SortedMap<String, PropertyPersistenceInfo> props) {
	super();
	this.table = table;
	this.type = type;
	this.props = props;
    }

    public String getTable() {
        return table;
    }
    public Class<? extends AbstractEntity<?>> getType() {
        return type;
    }
    public SortedMap<String, PropertyPersistenceInfo> getProps() {
        return props;
    }
}
