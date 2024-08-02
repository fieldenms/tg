package ua.com.fielden.platform.entity;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.isMockNotFoundType;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.isProxied;

final class CachingPropertyIndexerImpl extends PropertyIndexerImpl {

    /** Cache for lasting types (canonical entity types, generated entity types for centres, ...). */
    private final Cache<Class<? extends AbstractEntity<?>>, StandardIndex> lastingTypeCache;

    /** Cache for temporary types (generated for a temporary purpose). */
    private final Cache<Class<? extends AbstractEntity<?>>, StandardIndex> tmpTypeCache;

    CachingPropertyIndexerImpl(final CacheBuilder<Object, Object> lastingTypeCacheBuilder,
                               final CacheBuilder<Object, Object> tmpTypeCacheBuilder ) {
        this.lastingTypeCache = lastingTypeCacheBuilder.build();
        this.tmpTypeCache = tmpTypeCacheBuilder.build();
    }

    CachingPropertyIndexerImpl() {
        this(CacheBuilder.newBuilder()
                     .weakKeys() // classes can be compared with ==
                     .initialCapacity(512)
                     .maximumSize(8192)
                     .concurrencyLevel(50)
                     .expireAfterAccess(1, TimeUnit.DAYS),
             CacheBuilder.newBuilder()
                     .initialCapacity(512)
                     .maximumSize(8192)
                     .concurrencyLevel(50)
                     .expireAfterAccess(5, TimeUnit.MINUTES));
    }

    @Override
    public StandardIndex indexFor(final Class<? extends AbstractEntity<?>> entityType) {
        // performing a lookup in both caches first is faster than first determining which cache to use
        final var cachedInLasting = lastingTypeCache.getIfPresent(entityType);
        if (cachedInLasting != null) {
            return cachedInLasting;
        }
        final var cachedInTmp = tmpTypeCache.getIfPresent(entityType);
        if (cachedInTmp != null) {
            return cachedInTmp;
        }

        final Cache<Class<? extends AbstractEntity<?>>, StandardIndex> cache;
        if (isProxied(entityType) || isMockNotFoundType(entityType)) {
            cache = tmpTypeCache;
        }
        else {
            cache = lastingTypeCache;
        }
        return storeIndex(entityType, cache);
    }

    private StandardIndex storeIndex(final Class<? extends AbstractEntity<?>> entityType,
                                     final Cache<Class<? extends AbstractEntity<?>>, StandardIndex> cache) {
        final var newIndex = super.indexFor(entityType);
        cache.put(entityType, newIndex);
        return newIndex;
    }

}
