package ua.com.fielden.platform.entity;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.util.concurrent.ConcurrentHashMap;

interface PropertyIndexer {

    Index indexFor(final Class<? extends AbstractEntity<?>> entityType);

    interface Index {
        @Nullable MethodHandle getter(final String prop);

        @Nullable MethodHandle setter(final String prop);

    }

    // TODO concurrent with weak keys (Guava Cache)
    /**
     * Creates a caching indexer that delegates to the provided one.
     */
    static PropertyIndexer caching(final PropertyIndexer indexer) {
        final ConcurrentHashMap<Class<?>, Index> cache = new ConcurrentHashMap<>();
        return entityType -> cache.computeIfAbsent(entityType, $ -> indexer.indexFor(entityType));
    }

}
