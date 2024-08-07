package ua.com.fielden.platform.persistence.types;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

/**
 * <b>Warning</b>: This builder is mutable.
 */
public class HibernateTypeMappingsBuilder {

    private final Map<Class<?>, Object> map;

    public HibernateTypeMappingsBuilder() {
        map = new HashMap<>();
    }

    public HibernateTypeMappingsBuilder(HibernateTypeMappings mappings) {
        map = new HashMap<>(mappings.allMappings());
    }

    public HibernateTypeMappings build() {
        return new HibernateTypeMappingsImpl(ImmutableMap.copyOf(map));
    }

    public HibernateTypeMappingsBuilder put(final Class<?> type, final Object hibernateType) {
        map.put(type, hibernateType);
        return this;
    }

    public HibernateTypeMappingsBuilder putAll(final Map<? extends Class<?>, Object> map) {
        this.map.putAll(map);
        return this;
    }

    public HibernateTypeMappingsBuilder remove(final Class<?> type) {
        map.remove(type);
        return this;
    }

    public HibernateTypeMappingsBuilder removeAll(final Iterable<? extends Class<?>> types) {
        types.forEach(map::remove);
        return this;
    }

}
