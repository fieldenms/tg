package ua.com.fielden.platform.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;

public class EntityMetadata<ET extends AbstractEntity<?>> {
    private final String table;
    private final List<EntityResultQueryModel<ET>> models = new ArrayList<EntityResultQueryModel<ET>>();
    private final Class<ET> type;
    private final SortedMap<String, PropertyMetadata> props;

    public EntityMetadata(final String table, final Class<ET> type, final SortedMap<String, PropertyMetadata> props) {
	this(table, null, type, props);
    }

    public EntityMetadata(final List<EntityResultQueryModel<ET>> models, final Class<ET> type, final SortedMap<String, PropertyMetadata> props) {
	this(null, models, type, props);
    }

    public EntityMetadata(final Class<ET> type, final SortedMap<String, PropertyMetadata> props) {
	this(null, null, type, props);
    }

//    private static List<EntityResultQueryModel<ET>> produceModelIfAny(final Class<? extends AbstractEntity<?>> type) {
//	return new ArrayList<EntityResultQueryModel<? extends AbstractEntity<?>>>();
//    }

    private EntityMetadata(final String table, final List<EntityResultQueryModel<ET>> models, final Class<ET> type, final SortedMap<String, PropertyMetadata> props) {
	super();
	this.table = table;
	this.type = type;
	if (type == null) {
	    throw new IllegalArgumentException("Missing entity type!");
	}
	this.props = props;
	if (models != null) {
	    this.models.addAll(models);
	}
    }

    public boolean isPersisted() {
	return !StringUtils.isEmpty(table);
    }

    public String ddl() {
	final StringBuffer sb = new StringBuffer();
	sb.append("CREATE TABLE " + table + "(\n");
	for (final Iterator<Map.Entry<String, PropertyMetadata>> iterator = props.entrySet().iterator(); iterator.hasNext();) {
	    final Map.Entry<String, PropertyMetadata> entry = iterator.next();
	    if (entry.getValue().affectsMapping()) {
		sb.append(entry.getValue().ddl() + (iterator.hasNext() ? ",\n" : ""));
	    }
	}
	sb.append(")");

	return sb.toString();
    }

    public boolean isSynthetic() {
	return models.size() == 1;
    }

    public boolean isPure() {
	return !isPersisted() && !isSynthetic();
    }

    public boolean isOneToOne() {
	return isPersistedEntityType(getKeyType(type));
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

    public List<EntityResultQueryModel<ET>> getModels() {
        return models;
    }

    public static enum EntityCategory {
	PERSISTED, //
	QUERY_BASED, //
	UNION, //
	PURE;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((models == null) ? 0 : models.hashCode());
	result = prime * result + ((props == null) ? 0 : props.hashCode());
	result = prime * result + ((table == null) ? 0 : table.hashCode());
	result = prime * result + ((type == null) ? 0 : type.hashCode());
	return result;
    }

    public Set<String> getNotNullableProps() {
	final Set<String> result = new HashSet<String>();
	for (final Entry<String, PropertyMetadata> entry : props.entrySet()) {
	    if (!entry.getValue().isNullable()) {
		result.add(entry.getKey());
	    }
	}
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
	if (models == null) {
	    if (other.models != null) {
		return false;
	    }
	} else if (!models.equals(other.models)) {
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