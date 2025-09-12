package ua.com.fielden.platform.persistence.types;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Optional;

class HibernateTypeMappingsImpl implements HibernateTypeMappings {

    private final Map<Class<?>, Object> map;

    HibernateTypeMappingsImpl(final Map<? extends Class<?>, Object> map) {
        this.map = ImmutableMap.copyOf(map);
    }

    @Override
    public Optional<Object> getHibernateType(final Class<?> type) {
        return Optional.ofNullable(map.get(type));
    }

    @Override
    public Map<Class<?>, Object> allMappings() {
        return map;
    }

}
