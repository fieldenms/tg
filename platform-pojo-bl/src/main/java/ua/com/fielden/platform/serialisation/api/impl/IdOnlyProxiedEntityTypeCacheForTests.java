package ua.com.fielden.platform.serialisation.api.impl;

import jakarta.inject.Singleton;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxyEntity;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.serialisation.jackson.entities.OtherEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ua.com.fielden.platform.entity.proxy.EntityProxyContainer.proxy;

/**
 * {@link IIdOnlyProxiedEntityTypeCache} implementation for tests.
 * 
 * @author TG Team
 *
 */
@Singleton
public class IdOnlyProxiedEntityTypeCacheForTests implements IIdOnlyProxiedEntityTypeCache {

    private final Map<Class<? extends AbstractEntity<?>>, Class<? extends AbstractEntity<?>>> typesMap = buildMap();

    @Override
    public <T extends AbstractEntity<?>> Class<? extends T> getIdOnlyProxiedTypeFor(final Class<T> originalType) {
        return (Class<? extends T>) typesMap.get(originalType);
    }

    private Map<Class<? extends AbstractEntity<?>>, Class<? extends AbstractEntity<?>>> buildMap() {
        final Map<Class<? extends AbstractEntity<?>>, Class<? extends AbstractEntity<?>>> map = new HashMap<>();
        map.put(OtherEntity.class,
                proxy(OtherEntity.class, List.of("version", "key", "desc"), List.of(IIdOnlyProxyEntity.class)));
        map.put(TgPersistentEntityWithProperties.class,
                proxy(TgPersistentEntityWithProperties.class, List.of("version", "key", "desc"), List.of(IIdOnlyProxyEntity.class)));
        return map;
    }
}
