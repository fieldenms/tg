package ua.com.fielden.platform.eql.meta;

import static ua.com.fielden.platform.utils.EntityUtils.splitPropByFirstDot;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.dao.eql.EntityMetadata.EntityCategory;
import ua.com.fielden.platform.entity.AbstractEntity;
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
    public ResolutionPath resolve(final String dotNotatedPropName) {
        ResolutionPath result = new ResolutionPath();
        final Pair<String, String> parts = splitPropByFirstDot(dotNotatedPropName);
        final AbstractPropInfo foundPart = props.get(parts.getKey());
        if (foundPart != null) {
            result.add(foundPart);
            if (parts.getValue() != null) {
                result.add(foundPart.resolve(parts.getValue()));
            }
        }
        return result;
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