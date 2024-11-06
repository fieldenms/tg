package ua.com.fielden.platform.ioc;

import com.google.common.cache.CacheBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import ua.com.fielden.platform.basic.config.Workflows;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicPropertyAccess;
import ua.com.fielden.platform.entity.indexer.CacheConfig;
import ua.com.fielden.platform.entity.indexer.CachingPropertyIndexerImpl;
import ua.com.fielden.platform.entity.indexer.IPropertyIndexer;
import ua.com.fielden.platform.entity.indexer.PropertyIndexerImpl;
import ua.com.fielden.platform.parser.DurationParser;
import ua.com.fielden.platform.parser.IValueParser;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static ua.com.fielden.platform.parser.IValueParser.Result.ok;
import static ua.com.fielden.platform.parser.IValueParser.*;

/**
 * This module binds {@link DynamicPropertyAccess} and its dependencies.
 * <p>
 * This module can optionally be configured with {@link #options()}, which produces another <i>configuration</i> module
 * that needs to be explicitly installed.
 * Configuration is entirely optional.
 * <p>
 * <h4>Configurable caching</h4>
 * <p>
 * Performance of {@link DynamicPropertyAccess} heavily relies on caching.
 * This module supports cache configuration with {@link Properties}.
 * In practice, this means that {@code application.properties} file can be used to configure {@linkplain CacheConfig.CacheOptions cache parameters}.
 * <p>
 * There are two caches:
 * <ul>
 *     <li> one for primary entity types (those that exist throughout the lifetime of the application), and
 *     <li> another one for temporary entity types (generated for a temporary purpose).
 * </ul>
 *
 * <p>
 * Supported options:
 * <ul>
 *   <li> {@code dynamicPropertyAccess.caching} - one of {@link Options.Caching};
 *   <li> {@code dynamicPropertyAccess.typeCache} - configures the main type cache (see cache parameters below);
 *   <li> {@code dynamicPropertyAccess.tempTypeCache} - configures the temporary type cache (see cache parameters below).
 * </ul>
 * Cache parameters:
 * <ul>
 *   <li> {@code concurrencyLevel} - a non-negative decimal, see {@link CacheBuilder#concurrencyLevel(int)};
 *   <li> {@code maxSize} - a non-negative decimal, see {@link CacheBuilder#maximumSize(long)};
 *   <li> {@code expireAfterAccess} - a {@linkplain DurationParser duration specifier}, see {@link CacheBuilder#expireAfterAccess(Duration)};
 *   <li> {@code expireAfterWrite} - a {@linkplain DurationParser duration specifier}, see {@link CacheBuilder#expireAfterWrite(Duration)}.
 * </ul>
 *
 * Configuration example:
 * <pre>
 dynamicPropertyAccess.caching = ENABLED
 dynamicPropertyAccess.typeCache.concurrencyLevel = 100
 dynamicPropertyAccess.typeCache.expireAfterAccess = 12h
 dynamicPropertyAccess.tempTypeCache.maxSize = 2048
 dynamicPropertyAccess.tempTypeCache.expireAfterWrite = 10m
 * </pre>
 */
public final class DynamicPropertyAccessIocModule extends AbstractPlatformIocModule {


    public static final class Options extends AbstractModule {

        public static final String PROPERTY_PREFIX = "dynamicPropertyAccess";
        public static final String MAIN_CACHE_PROPERTY_PREFIX = String.join(".", PROPERTY_PREFIX, "typeCache");
        public static final String TEMPORARY_CACHE_PROPERTY_PREFIX = String.join(".", PROPERTY_PREFIX, "tempTypeCache");

        /**
         * @param prefix  one of {@link #MAIN_CACHE_PROPERTY_PREFIX}, {@link #TEMPORARY_CACHE_PROPERTY_PREFIX}
         */
        public static String cachePropertyName(final String prefix, final CacheConfig.CacheOptions option) {
            return prefix + '.' + option.name();
        }

        public static final String CACHING_PROPERTY = String.join(".", PROPERTY_PREFIX, "caching");
        private final IValueParser<Properties, Caching> cachingPropertyParser =
                propertyParser(CACHING_PROPERTY, enumIgnoreCaseParser(Caching.values()), Caching.AUTO);

        public final Caching caching;
        public final CacheConfig mainCacheConfig;
        public final CacheConfig tmpCacheConfig;

        public enum Caching { ENABLED, DISABLED, AUTO }

        private Options(final Caching caching, final CacheConfig mainCacheConfig, final CacheConfig tmpCacheConfig) {
            this.mainCacheConfig = mainCacheConfig;
            this.tmpCacheConfig = tmpCacheConfig;
            requireNonNull(caching);
            this.caching = caching;
        }

        @Override
        protected void configure() {
            bind(Options.class).toInstance(this);
        }

        public Options fromProperties(final Properties properties) {
            return new Options(cachingPropertyParser.apply(properties).getOrThrow(),
                               CacheConfig.fromProperties(properties, MAIN_CACHE_PROPERTY_PREFIX),
                               CacheConfig.fromProperties(properties, TEMPORARY_CACHE_PROPERTY_PREFIX));
        }

        public Options fromMap(final Map<String, ?> map) {
            final var properties = new Properties();
            properties.putAll(map);
            return fromProperties(properties);
        }

        /**
         * Explicitly controls the use of caching, bypassing the standard choice based on the active {@linkplain Workflows workflow}.
         */
        public Options caching(final Caching value) {
            return new Options(value, mainCacheConfig, tmpCacheConfig);
        }

        public Options configureMainCache(final CacheConfig config) {
            return new Options(caching, config, tmpCacheConfig);
        }

        public Options configureMainCache(final Properties properties) {
            return new Options(caching, CacheConfig.fromProperties(properties, MAIN_CACHE_PROPERTY_PREFIX), tmpCacheConfig);
        }

        public Options configureTemporaryCache(final CacheConfig config) {
            return new Options(caching, mainCacheConfig, config);
        }

        public Options configureTemporaryCache(final Properties properties) {
            return new Options(caching, mainCacheConfig, CacheConfig.fromProperties(properties, TEMPORARY_CACHE_PROPERTY_PREFIX));
        }
    }

    public static Options options() {
        return new Options(Options.Caching.AUTO, CacheConfig.EMPTY, CacheConfig.EMPTY);
    }

    private Options getOptions(final Injector injector) {
        if (injector.getExistingBinding(Key.get(Options.class)) != null) {
            return injector.getInstance(Options.class);
        } else {
            return options();
        }
    }

    @Override
    protected void configure() {
        requestStaticInjection(AbstractEntity.class);
    }

    @Provides
    IPropertyIndexer providePropertyIndexer(final Workflows workflow, final Injector injector) {
        final Options options = getOptions(injector);
        return switch (options.caching) {
            case ENABLED -> new CachingPropertyIndexerImpl(options.mainCacheConfig, options.tmpCacheConfig);
            case DISABLED -> new PropertyIndexerImpl();
            case AUTO -> switch (workflow) {
                case deployment, vulcanizing -> new CachingPropertyIndexerImpl(options.mainCacheConfig, options.tmpCacheConfig);
                // Caching during development can be enabled if we can guarantee that it won't get in the way of redefining entity types at runtime.
                // So which entity types can be redefined?
                // 1. Canonical entity types – if HotSpot is used, then no, because it doesn't support structural changes.
                //    If JBR (JetBrains Runtime) is used, then canonical entity types can indeed be redefined: properties can be removed, added and modified.
                //    However, the implications of such changes are far too wide, and they would also affect other parts of the system (e.g., metadata).
                //    Therefore, since this is not supported yet, we need not worry about it here.
                // 2. Generated entity types – any live changes should result in generation of new entity types (e.g., modifying an entity centre configuration).
                //    Since those types will be new, old cached types would not get in the way.
                // Therefore, for now caching can be used in the development workflow.
                case development -> new CachingPropertyIndexerImpl(options.mainCacheConfig, options.tmpCacheConfig);
            };
        };
    }

}
