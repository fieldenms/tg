package ua.com.fielden.platform.eql.meta;

import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public class ComponentInfo implements IResolvable {
    private final Class<?> javaType;
    private final SortedMap<String, AbstractPropInfo> props = new TreeMap<>();

    public ComponentInfo(final Class<? extends AbstractEntity<?>> javaType) {
        this.javaType = javaType;
    }

    public AbstractPropInfo resolve(final String dotNotatedPropName) {
        final Pair<String, String> parts = EntityUtils.splitPropByFirstDot(dotNotatedPropName);
        final AbstractPropInfo foundPart = props.get(parts.getKey());
        return foundPart == null ? null : foundPart.resolve(parts.getValue());
    }

    protected SortedMap<String, AbstractPropInfo> getProps() {
        return props;
    }

    protected Class<?> getJavaType() {
        return javaType;
    }

    @Override
    public String toString() {
        return javaType.getSimpleName();
    }

    @Override
    public Class javaType() {
        return javaType;
    }
}