package ua.com.fielden.platform.eql.meta;

import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.dao.eql.EntityMetadata.EntityCategory;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public class EntityInfo implements IResolvable {
    private final Class<? extends AbstractEntity<?>> javaType;
    private final SortedMap<String, AbstractPropInfo> props = new TreeMap<>();
    private final EntityCategory category;

    public EntityInfo(final Class<? extends AbstractEntity<?>> javaType, final EntityCategory category) {
        this.javaType = javaType;
        this.category = category;
    }

    @Override
    public AbstractPropInfo resolve(final String dotNotatedPropName) {
        final Pair<String, String> parts = EntityUtils.splitPropByFirstDot(dotNotatedPropName);
        final AbstractPropInfo foundPart = props.get(parts.getKey());
        return foundPart == null ? null : foundPart.resolve(parts.getValue());
    }

    protected SortedMap<String, AbstractPropInfo> getProps() {
        return props;
    }

    @Override
    public String toString() {
        return javaType.getSimpleName();
    }

    @Override
    public Class javaType() {
        return javaType;
    }

    public EntityCategory getCategory() {
        return category;
    }
}