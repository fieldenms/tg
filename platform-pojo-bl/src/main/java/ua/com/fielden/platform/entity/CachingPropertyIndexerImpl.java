package ua.com.fielden.platform.entity;

import java.util.concurrent.ConcurrentHashMap;

final class CachingPropertyIndexerImpl extends PropertyIndexerImpl {

    // TODO Guava Cache with smart eviction strategy
    private final ConcurrentHashMap<Class<?>, StandardIndex> cache = new ConcurrentHashMap<>();

    @Override
    public StandardIndex indexFor(final Class<? extends AbstractEntity<?>> entityType) {
        final var cached = cache.get(entityType);
        if (cached != null) {
            return cached;
        }
        final var newIndex = super.indexFor(entityType);
        cache.put(entityType, newIndex);
        return newIndex;
    }

}
