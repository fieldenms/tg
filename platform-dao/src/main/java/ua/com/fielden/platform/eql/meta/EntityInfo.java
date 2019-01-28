package ua.com.fielden.platform.eql.meta;

import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public class EntityInfo<T extends AbstractEntity<?>> implements IResolvable<T> {
    private final Class<T> javaType;
    private final SortedMap<String, AbstractPropInfo<?, ?>> props = new TreeMap<>();
    private final EntityCategory category;

    public EntityInfo(final Class<T> javaType, final EntityCategory category) {
        this.javaType = javaType;
        this.category = category;
    }

    @Override
    public AbstractPropInfo<?, ?> resolve(final String dotNotatedPropName) {
        final Pair<String, String> parts = EntityUtils.splitPropByFirstDot(dotNotatedPropName);
        final AbstractPropInfo<?, ?> foundPart = props.get(parts.getKey());
        return foundPart == null ? null : foundPart.resolve(parts.getValue());
    }

    public EntityInfo<T> addProp(final AbstractPropInfo<?, ?> propInfo) { 
        props.put(propInfo.getName(), propInfo);
        return this;
    }
    
    public SortedMap<String, AbstractPropInfo<?, ?>> getProps() {
        return props;
    }
    
    @Override
    public String toString() {
        return javaType.getSimpleName();
    }

    @Override
    public Class<T> javaType() {
        return javaType;
    }

    public EntityCategory getCategory() {
        return category;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((category == null) ? 0 : category.hashCode());
        result = prime * result + ((javaType == null) ? 0 : javaType.hashCode());
        result = prime * result + props.keySet().hashCode();
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
        if (!(obj instanceof EntityInfo)) {
            return false;
        }
        final EntityInfo other = (EntityInfo) obj;
        if (category != other.category) {
            return false;
        }
        if (javaType == null) {
            if (other.javaType != null) {
                return false;
            }
        } else if (!javaType.equals(other.javaType)) {
            return false;
        }
        
        if (!props.keySet().equals(other.props.keySet())) {
            return false;
        }
        return true;
    }
}