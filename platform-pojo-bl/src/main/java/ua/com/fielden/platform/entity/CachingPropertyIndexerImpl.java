package ua.com.fielden.platform.entity;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import ua.com.fielden.platform.entity.DynamicPropertyAccessModule.CacheConfig;

import java.lang.invoke.MethodHandle;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.isMockNotFoundType;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.isProxied;

/**
 * <h4> On weak keys
 * <p>
 * Cache of the type used in this class won't benefit from weak keys due to key values ({@link Class} instances) being
 * strongly referenced by cached values: through {@link MethodHandle} in {@link StandardIndex}
 * ({@link java.lang.invoke.DirectMethodHandle#member} -> {@link java.lang.invoke.MemberName#clazz}).
 * <p>
 * Therefore, a time-based eviction strategy is required.
 */
final class CachingPropertyIndexerImpl extends PropertyIndexerImpl {

    /** Cache for lasting types (canonical entity types, generated entity types for centres, ...). */
    private final Cache<Class<? extends AbstractEntity<?>>, StandardIndex> lastingTypeCache;

    /** Cache for temporary types (generated for a temporary purpose). */
    private final Cache<Class<? extends AbstractEntity<?>>, StandardIndex> tmpTypeCache;

    CachingPropertyIndexerImpl(final CacheConfig lastingTypeCacheConf, final CacheConfig tmpTypeCacheConf) {
        this.lastingTypeCache = lastingTypeCacheConf.apply(
                        CacheBuilder.newBuilder()
                                .initialCapacity(512)
                                .maximumSize(8192)
                                .concurrencyLevel(50)
                                .expireAfterAccess(1, TimeUnit.DAYS))
                .build();
        this.tmpTypeCache = tmpTypeCacheConf.apply(
                        CacheBuilder.newBuilder()
                                .initialCapacity(512)
                                .maximumSize(8192)
                                .concurrencyLevel(50)
                                .expireAfterAccess(5, TimeUnit.MINUTES))
                .build();
    }

    CachingPropertyIndexerImpl() {
        this(CacheConfig.identity(), CacheConfig.identity());
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
