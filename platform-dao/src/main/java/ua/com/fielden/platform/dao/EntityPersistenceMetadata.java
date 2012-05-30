package ua.com.fielden.platform.dao;

import java.util.SortedMap;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

public class EntityPersistenceMetadata {
    private final String table;
    private final EntityResultQueryModel<? extends AbstractEntity<?>> model;
    private final Class<? extends AbstractEntity<?>> type;
    private final SortedMap<String, PropertyPersistenceInfo> props;

    public EntityPersistenceMetadata(final String table, final Class<? extends AbstractEntity<?>> type, final SortedMap<String, PropertyPersistenceInfo> props) {
	this(table, null, type, props);
    }

    public EntityPersistenceMetadata(final EntityResultQueryModel<? extends AbstractEntity<?>> model, final Class<? extends AbstractEntity<?>> type, final SortedMap<String, PropertyPersistenceInfo> props) {
	this(null, model, type, props);
    }

    private EntityPersistenceMetadata(final String table, final EntityResultQueryModel<? extends AbstractEntity<?>> model, final Class<? extends AbstractEntity<?>> type, final SortedMap<String, PropertyPersistenceInfo> props) {
	super();
	this.table = table;
	this.type = type;
	this.props = props;
	this.model = model;
    }

    public boolean isPersisted() {
	return !StringUtils.isEmpty(table);
    }

    public boolean isSynthetic() {
	return model != null;
    }

    public boolean isDefinitionOnly() {
	return !isPersisted() && !isSynthetic();
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

    public EntityResultQueryModel<? extends AbstractEntity<?>> getModel() {
        return model;
    }
}