package ua.com.fielden.platform.entity.query.metadata;

import java.util.SortedMap;

import ua.com.fielden.platform.entity.AbstractEntity;

public abstract class AbstractEntityMetadata<ET extends AbstractEntity<?>> {
    public final Class<ET> type;
    private final SortedMap<String, PropertyMetadata> props;

    protected AbstractEntityMetadata(final Class<ET> type, final SortedMap<String, PropertyMetadata> props) {
        this.type = type;
        if (type == null) {
            throw new IllegalArgumentException("Missing entity type!");
        }
        this.props = props;
    }

    public SortedMap<String, PropertyMetadata> getProps() {
        return props;
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
}