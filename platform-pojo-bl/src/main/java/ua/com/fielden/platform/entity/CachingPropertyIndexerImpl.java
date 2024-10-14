package ua.com.fielden.platform.entity;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import ua.com.fielden.platform.entity.DynamicPropertyAccessModule.CacheConfig;

import java.lang.invoke.MethodHandle;
import java.time.Duration;
import java.util.concurrent.ExecutionException;

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
    private final Cache<Class<? extends AbstractEntity<?>>, StandardIndex> mainTypeCache;

    /** Cache for temporary types (generated for a temporary purpose). */
    private final Cache<Class<? extends AbstractEntity<?>>, StandardIndex> tmpTypeCache;

    CachingPropertyIndexerImpl(final CacheConfig mainTypeCacheConf, final CacheConfig tmpTypeCacheConf) {
        this.mainTypeCache = buildCache(CacheConfig.rightMerge(defaultMainTypeCacheConfig(), mainTypeCacheConf));
        this.tmpTypeCache = buildCache(CacheConfig.rightMerge(defaultTmpTypeCacheConfig(), tmpTypeCacheConf));
    }

    CachingPropertyIndexerImpl() {
        this(CacheConfig.EMPTY, CacheConfig.EMPTY);
    }

    private CacheConfig defaultMainTypeCacheConfig() {
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
        // First determine which cache to use so that an atomic operation can be performed on it.
        final Cache<Class<? extends AbstractEntity<?>>, StandardIndex> cache;
        if (isProxied(entityType) || isMockNotFoundType(entityType)) {
            cache = tmpTypeCache;
        }
        else {
            cache = mainTypeCache;
        }

        try {
            // super.indexFor may invoke this method recursively to build an index for the supertype of this entity type,
            // causing a recursive load in the cache, which is ok as long as values are loaded for *different* keys
            // (recursive loading of a value for the same key is unsupported by Guava Cache).
            return cache.get(entityType, () -> super.indexFor(entityType));
        } catch (final ExecutionException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

}
