package ua.com.fielden.platform.entity;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

final class CachingPropertyIndexerImpl extends PropertyIndexerImpl {

    private final Cache<Class<? extends AbstractEntity<?>>, StandardIndex> cache;

    CachingPropertyIndexerImpl(final CacheBuilder<Object, Object> cacheBuilder) {
        this.cache = cacheBuilder.build();
    }

    CachingPropertyIndexerImpl() {
        this(CacheBuilder.newBuilder()
                     .weakKeys() // classes can be compared with ==
                     .initialCapacity(512)
                     .maximumSize(8192)
                     .concurrencyLevel(50)
                     .expireAfterAccess(1, TimeUnit.DAYS));
    }

    @Override
    public StandardIndex indexFor(final Class<? extends AbstractEntity<?>> entityType) {
        // recursive updates are frown upon, so let's do it by hand
        // see https://github.com/prestodb/presto/pull/4980
        final var cached = cache.getIfPresent(entityType);
        if (cached != null) {
            return cached;
        }
        final var newIndex = super.indexFor(entityType);
        cache.put(entityType, newIndex);
        return newIndex;
    }

}
