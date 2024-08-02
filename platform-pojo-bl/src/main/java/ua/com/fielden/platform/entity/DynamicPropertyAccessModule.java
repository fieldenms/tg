package ua.com.fielden.platform.entity;

import com.google.common.cache.CacheBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import ua.com.fielden.platform.basic.config.Workflows;
import ua.com.fielden.platform.parser.ValueParser;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static ua.com.fielden.platform.parser.ValueParser.Result.ok;
import static ua.com.fielden.platform.parser.ValueParser.*;

public final class DynamicPropertyAccessModule extends AbstractModule {

    public enum CacheOptions {

        concurrencyLevel(intParser().and(i -> ok(builder -> builder.concurrencyLevel(i)))),
        maxSize(longParser().and(l -> ok(builder -> builder.maximumSize(l)))),
        ;

        public final ValueParser<Object, CacheConfig> parser;

        CacheOptions(final ValueParser<Object, CacheConfig> parser) {
            this.parser = parser;
        }
    }

    public static final class Options extends AbstractModule {

        public static final String PROPERTY_PREFIX = "dynamicPropertyAccess";
        public static final String LASTING_CACHE_PROPERTY_PREFIX = String.join(".", PROPERTY_PREFIX, "typeCache");
        public static final String TEMPORARY_CACHE_PROPERTY_PREFIX = String.join(".", PROPERTY_PREFIX, "tmpTypeCache");

        /**
         * @param prefix  one of {@link #LASTING_CACHE_PROPERTY_PREFIX}, {@link #TEMPORARY_CACHE_PROPERTY_PREFIX}
         */
        public static String cachePropertyName(final String prefix, final CacheOptions option) {
            return prefix + '.' + option.name();
        }

        public static final String CACHING_PROPERTY = String.join(".", PROPERTY_PREFIX, "caching");
        private final ValueParser<Properties, Caching> cachingPropertyParser =
                propertyParser(CACHING_PROPERTY, enumIgnoreCaseParser(Caching.values()), Caching.AUTO);

        public final Caching caching;
        public final CacheConfig lastingCacheConfig;
        public final CacheConfig tmpCacheConfig;

        public enum Caching { ENABLED, DISABLED, AUTO }

        private Options(final Caching caching, final CacheConfig lastingCacheConfig, final CacheConfig tmpCacheConfig) {
            this.lastingCacheConfig = lastingCacheConfig;
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
                               CacheConfig.fromProperties(properties, LASTING_CACHE_PROPERTY_PREFIX),
                               CacheConfig.fromProperties(properties, TEMPORARY_CACHE_PROPERTY_PREFIX));
        }

        public Options fromMap(final Map<?, ?> map) {
            final var properties = new Properties();
            properties.putAll(map);
            return fromProperties(properties);
        }

        /**
         * Explicitly controls the use of caching, bypassing the standard choice based on the active {@linkplain Workflows workflow}.
         */
        public Options caching(final Caching value) {
            return new Options(value, lastingCacheConfig, tmpCacheConfig);
        }

        public Options configureLastingCache(final CacheConfig config) {
            return new Options(caching, config, tmpCacheConfig);
        }

        public Options configureLastingCache(final Properties properties) {
            return new Options(caching, CacheConfig.fromProperties(properties, LASTING_CACHE_PROPERTY_PREFIX), tmpCacheConfig);
        }

        public Options configureTemporaryCache(final CacheConfig config) {
            return new Options(caching, lastingCacheConfig, config);
        }

        public Options configureTemporaryCache(final Properties properties) {
            return new Options(caching, lastingCacheConfig, CacheConfig.fromProperties(properties, TEMPORARY_CACHE_PROPERTY_PREFIX));
        }
    }

    public static Options options() {
        return new Options(Options.Caching.AUTO, CacheConfig.identity(), CacheConfig.identity());
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
    PropertyIndexer providePropertyIndexer(final Workflows workflow, final Injector injector) {
        final Options options = getOptions(injector);
        return switch (options.caching) {
            case ENABLED -> new CachingPropertyIndexerImpl(options.lastingCacheConfig, options.tmpCacheConfig);
            case DISABLED -> new PropertyIndexerImpl();
            case AUTO -> switch (workflow) {
                case deployment, vulcanizing -> new CachingPropertyIndexerImpl(options.lastingCacheConfig, options.tmpCacheConfig);
                /*
                 Caching during development can be enabled if we can guarantee that it won't get in the way of redefining
                 entity types at runtime. So which entity types can be redefined?
                 * Canonical entity types - if HotSpot is used, then no, because it doesn't support structural changes.
                 If JBR (JetBrains Runtime) is used, then canonical entity types can indeed be redefined: properties
                 can be removed, added and modified. However, the implications of such changes are far too wide, they
                 would also affect other parts of the system (e.g., metadata). Therefore, since this isn't supported
                 yet, we need not worry about it here.
                 * Generated entity types - any live changes should result in generation of new entity types (e.g.,
                 modifying an entity centre configuration). Since those types will be new, old cached types won't get
                 in the way.
                */
                case development -> new CachingPropertyIndexerImpl(options.lastingCacheConfig, options.tmpCacheConfig);
            };
        };
    }

    public interface CacheConfig extends Function<CacheBuilder<Object, Object>, CacheBuilder<Object, Object>> {

        default CacheConfig combine(final CacheConfig config) {
            return builder -> config.apply(this.apply(builder));
        }

        static CacheConfig identity() {
            return x -> x;
        }

        static CacheConfig fromProperties(final java.util.Properties properties, final String prefix) {
            return Arrays.stream(CacheOptions.values()).map(opt -> {
                final String propName = prefix + "." + opt.name();
                return optPropertyParser(propName, opt.parser).apply(properties).getOrThrow();
            }).flatMap(Optional::stream).reduce(identity(), CacheConfig::combine);
        }
    }

}
