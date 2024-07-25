package ua.com.fielden.platform.entity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

interface PropertyIndexer {

    Index indexFor(final Class<? extends AbstractEntity<?>> entityType);

    /**
     * @param propertyHandles  for reading property values, keyed on property names
     * @param setters  for writing property values, keyed on property names
     */
    record Index(Map<String, VarHandle> propertyHandles,
                 Map<String, MethodHandle> setters)
    {}

    // TODO concurrent with weak keys (Guava Cache)
    /**
     * Creates a caching indexer that delegates to the provided one.
     */
    static PropertyIndexer caching(final PropertyIndexer indexer) {
        final ConcurrentHashMap<Class<?>, Index> cache = new ConcurrentHashMap<>();
        return entityType -> cache.computeIfAbsent(entityType, $ -> indexer.indexFor(entityType));
    }

}
