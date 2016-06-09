package ua.com.fielden.platform.dao;

import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import ua.com.fielden.platform.entity.AbstractEntity;

public abstract class AbstractEntityMetadata<ET extends AbstractEntity<?>> {
    private final Class<ET> type;
    private final SortedMap<String, PropertyMetadata> props;

    protected AbstractEntityMetadata(final Class<ET> type, final SortedMap<String, PropertyMetadata> props) {
        super();
        this.type = type;
        if (type == null) {
            throw new IllegalArgumentException("Missing entity type!");
        }
        this.props = props;
    }

    public boolean isOneToOne() {
        return isPersistedEntityType(getKeyType(type));
    }

    public Class<? extends AbstractEntity<?>> getType() {
        return type;
    }

    public SortedMap<String, PropertyMetadata> getProps() {
        return props;
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((props == null) ? 0 : props.hashCode());
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
        if (!(obj instanceof AbstractEntityMetadata)) {
            return false;
        }
        final AbstractEntityMetadata other = (AbstractEntityMetadata) obj;
        if (props == null) {
            if (other.props != null) {
                return false;
            }
        } else if (!props.equals(other.props)) {
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

    public int countCalculatedProps() {
        int result = 0;
        for (final PropertyMetadata propMetadata : props.values()) {
            if (propMetadata.isCalculated()) {
                result = result + 1;
            }
        }
        return result;
    }

    public int countCollectionalProps() {
        int result = 0;
        for (final PropertyMetadata propMetadata : props.values()) {
            if (propMetadata.isCollection()) {
                result = result + 1;
            }
        }
        return result;
    }

}