package ua.com.fielden.platform.serialisation.api.impl;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.proxy.EntityProxyContainer;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.serialisation.jackson.entities.OtherEntity;

/**
 * {@link IIdOnlyProxiedEntityTypeCache} implementation for tests.
 * 
 * @author TG Team
 *
 */
public class IdOnlyProxiedEntityTypeCacheForTests implements IIdOnlyProxiedEntityTypeCache {

    private final Map<Class<? extends AbstractEntity<?>>, Class<? extends AbstractEntity<?>>> typesMap = buildMap();

    @Override
    public <T extends AbstractEntity<?>> Class<? extends T> getIdOnlyProxiedTypeFor(final Class<T> originalType) {
        return (Class<? extends T>) typesMap.get(originalType);
    }

    private Map<Class<? extends AbstractEntity<?>>, Class<? extends AbstractEntity<?>>> buildMap() {
        final Map<Class<? extends AbstractEntity<?>>, Class<? extends AbstractEntity<?>>> map = new HashMap<>();
        map.put(OtherEntity.class, EntityProxyContainer.proxy(OtherEntity.class, "version", "key", "desc"));
        return map;
    }
}