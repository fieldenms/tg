package ua.com.fielden.platform.entity;

import java.util.concurrent.ConcurrentHashMap;

final class CachingPropertyIndexerImpl extends PropertyIndexerImpl {

    // TODO Guava Cache with smart eviction strategy
    private final ConcurrentHashMap<Class<?>, Index> cache = new ConcurrentHashMap<>();

    @Override
    public Index indexFor(final Class<? extends AbstractEntity<?>> entityType) {
        return cache.computeIfAbsent(entityType, $ -> super.indexFor(entityType));
    }

}
