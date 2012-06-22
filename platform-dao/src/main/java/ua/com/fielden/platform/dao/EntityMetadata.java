package ua.com.fielden.platform.dao;

import java.util.SortedMap;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

public class EntityMetadata {
    private final String table;
    private final EntityResultQueryModel<? extends AbstractEntity<?>> model;
    private final Class<? extends AbstractEntity<?>> type;
    private final SortedMap<String, PropertyMetadata> props;

    public EntityMetadata(final String table, final Class<? extends AbstractEntity<?>> type, final SortedMap<String, PropertyMetadata> props) {
	this(table, null, type, props);
    }

    public EntityMetadata(final EntityResultQueryModel<? extends AbstractEntity<?>> model, final Class<? extends AbstractEntity<?>> type, final SortedMap<String, PropertyMetadata> props) {
	this(null, model, type, props);
    }

    public EntityMetadata(final Class<? extends AbstractEntity<?>> type, final SortedMap<String, PropertyMetadata> props) {
	this(null, null, type, props);
    }

    private EntityMetadata(final String table, final EntityResultQueryModel<? extends AbstractEntity<?>> model, final Class<? extends AbstractEntity<?>> type, final SortedMap<String, PropertyMetadata> props) {
	super();
	this.table = table;
	this.type = type;
	if (type == null) {
	    throw new IllegalArgumentException("Missing entity type!");
	}
	this.props = props;
	this.model = model;
    }

    public boolean isPersisted() {
	return !StringUtils.isEmpty(table);
    }

    public boolean isSynthetic() {
	return model != null;
    }

    public boolean isPure() {
	return !isPersisted() && !isSynthetic();
    }

    public String getTable() {
        return table;
    }
    public Class<? extends AbstractEntity<?>> getType() {
        return type;
    }
    public SortedMap<String, PropertyMetadata> getProps() {
        return props;
    }

    public EntityResultQueryModel<? extends AbstractEntity<?>> getModel() {
        return model;
    }

    public static enum EntityCategory {
	PERSISTED, //
	CALCULATED, //
	UNION, //
	PURE;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((model == null) ? 0 : model.hashCode());
	result = prime * result + ((props == null) ? 0 : props.hashCode());
	result = prime * result + ((table == null) ? 0 : table.hashCode());
	result = prime * result + ((type == null) ? 0 : type.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (!(obj instanceof EntityMetadata)) {
	    return false;
	}
	final EntityMetadata other = (EntityMetadata) obj;
	if (model == null) {
	    if (other.model != null) {
		return false;
	    }
	} else if (!model.equals(other.model)) {
	    return false;
	}
	if (props == null) {
	    if (other.props != null) {
		return false;
	    }
	} else if (!props.equals(other.props)) {
	    return false;
	}
	if (table == null) {
	    if (other.table != null) {
		return false;
	    }
	} else if (!table.equals(other.table)) {
	    return false;
	}
	if (type == null) {
	    if (other.type != null) {
		return false;
	    }
	} else if (!type.equals(other.type)) {
	    return false;
	}
	return true;
    }
}