package ua.com.fielden.platform.entity;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import ua.com.fielden.platform.entity.DynamicPropertyAccessModule.CacheConfig;

import java.lang.invoke.MethodHandle;
import java.time.Duration;

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
        this.lastingTypeCache = buildCache(CacheConfig.rightMerge(defaultLastingTypeCacheConfig(), lastingTypeCacheConf));
        this.tmpTypeCache = buildCache(CacheConfig.rightMerge(defaultTmpTypeCacheConfig(), tmpTypeCacheConf));
    }

    CachingPropertyIndexerImpl() {
        this(CacheConfig.EMPTY, CacheConfig.EMPTY);
    }

    private CacheConfig defaultLastingTypeCacheConfig() {
        return CacheConfig.EMPTY.concurrencyLevel(50).maxSize(8192).expireAfterAccess(Duration.ofDays(1));
    }

    private CacheConfig defaultTmpTypeCacheConfig() {
        return CacheConfig.EMPTY.concurrencyLevel(50).maxSize(8192).expireAfterAccess(Duration.ofMinutes(5));
    }

    private static <K, V> Cache<K, V> buildCache(final CacheConfig config) {
        var builder = CacheBuilder.newBuilder().initialCapacity(512);
        builder = config.maxSize.isPresent() ? builder.maximumSize(config.maxSize.getAsLong()) : builder;
        builder = config.concurrencyLevel.isPresent() ? builder.concurrencyLevel(config.concurrencyLevel.getAsInt()) : builder;
        builder = config.expireAfterAccess.isPresent() ? builder.expireAfterAccess(config.expireAfterAccess.get()) : builder;
        builder = config.expireAfterWrite.isPresent() ? builder.expireAfterWrite(config.expireAfterWrite.get()) : builder;
        return builder.build();
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
