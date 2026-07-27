package ua.com.fielden.platform.migration;

import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/// A cache for entities.
///
/// Each entity type is associated with a cache that has type `Map<Object, Long>`.
///
/// Each cache key (`Object`) is the value of an entity key.
/// * For simple keys, the cache key is the value of property `key`.
/// * For composite keys with a single member, the cache key is the value of that member.
/// * For composite keys with multiple members, the cache key is a list of values for those members.
///
/// Each cache value (`Long`) is an entity ID.
///
final class IdCache {

    private final Map<Class<?>, Map<Object, Long>> cache;

    IdCache() {
        cache = new HashMap<>();
    }

    public Optional<Map<Object, Long>> cacheFor(final Class<? extends AbstractEntity<?>> entityType) {
        return Optional.ofNullable(cache.get(entityType));
    }

    public Map<Object, Long> cacheFor(
            final Class<? extends AbstractEntity<?>> entityType,
            final Supplier<? extends Map<Object, Long>> cacheSupplier)
    {
        return cache.computeIfAbsent(entityType, $ -> cacheSupplier.get());
    }

    public void registerCacheForType(final Class<? extends AbstractEntity<?>> entityType) {
        if (!cache.containsKey(entityType)) {
            cache.put(entityType, new HashMap<>());
        }
    }

}
