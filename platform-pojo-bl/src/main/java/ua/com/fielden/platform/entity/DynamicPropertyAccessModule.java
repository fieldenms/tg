package ua.com.fielden.platform.entity;

import com.google.common.cache.CacheBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.basic.config.Workflows;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.parser.ValueParser;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static ua.com.fielden.platform.parser.ValueParser.Result.ok;
import static ua.com.fielden.platform.parser.ValueParser.*;

/**
 * This module binds {@link DynamicPropertyAccess} and its dependencies.
 * <p>
 * This module can optionally be configured with {@link #options()}, which produces another <i>configuration</i> module
 * that needs to be explicitly installed. Configuration is entirely optional.
 * <p>
 * <h4> Configurable caching
 * <p>
 * Performance of {@link DynamicPropertyAccess} heavily relies on caching. This module supports cache configuration
 * with {@link Properties}. In practice, this means that {@code application.properties} file can be used to configure
 * {@linkplain CacheOptions cache parameters}.
 * <p>
 * There are 2 caches: one for primary entity types (those that exist throughout the lifetime of the application)
 * and another one for temporary entity types (generated for a temporary purpose).
 * <p>
 * Supported options:
 * <ul>
 *   <li> {@code dynamicPropertyAccess.caching} - one of {@link Options.Caching}
 *   <li> {@code dynamicPropertyAccess.typeCache} - configures the main type cache (see cache parameters below)
 *   <li> {@code dynamicPropertyAccess.tempTypeCache} - configures the temporary type cache (see cache parameters below)
 * </ul>
 * Cache parameters:
 * <ul>
 *   <li> {@code concurrencyLevel} - a non-negative decimal, see {@link CacheBuilder#concurrencyLevel(int)}
 *   <li> {@code maxSize} - a non-negative decimal, see {@link CacheBuilder#maximumSize(long)}
 *   <li> {@code expireAfterAccess} - a {@linkplain DurationParser duration specifier}, see {@link CacheBuilder#expireAfterAccess(Duration)}
 *   <li> {@code expireAfterWrite} - a {@linkplain DurationParser duration specifier}, see {@link CacheBuilder#expireAfterWrite(Duration)}
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
public final class DynamicPropertyAccessModule extends AbstractModule {

    public enum CacheOptions {

        concurrencyLevel(intParser().and(i -> ok(cfg -> cfg.concurrencyLevel(i)))),
        maxSize(longParser().and(l -> ok(cfg -> cfg.maxSize(l)))),
        expireAfterAccess(new DurationParser().and(duration -> ok(cfg -> cfg.expireAfterAccess(duration)))),
        expireAfterWrite(new DurationParser().and(duration -> ok(cfg -> cfg.expireAfterWrite(duration)))),
        ;

        public final ValueParser<Object, Function<CacheConfig, CacheConfig>> parser;

        CacheOptions(final ValueParser<Object, Function<CacheConfig, CacheConfig>> parser) {
            this.parser = parser;
        }
    }

    public static final class Options extends AbstractModule {

        public static final String PROPERTY_PREFIX = "dynamicPropertyAccess";
        public static final String MAIN_CACHE_PROPERTY_PREFIX = String.join(".", PROPERTY_PREFIX, "typeCache");
        public static final String TEMPORARY_CACHE_PROPERTY_PREFIX = String.join(".", PROPERTY_PREFIX, "tempTypeCache");

        /**
         * @param prefix  one of {@link #MAIN_CACHE_PROPERTY_PREFIX}, {@link #TEMPORARY_CACHE_PROPERTY_PREFIX}
         */
        public static String cachePropertyName(final String prefix, final CacheOptions option) {
            return prefix + '.' + option.name();
        }

        public static final String CACHING_PROPERTY = String.join(".", PROPERTY_PREFIX, "caching");
        private final ValueParser<Properties, Caching> cachingPropertyParser =
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

        public Options fromMap(final Map<?, ?> map) {
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
    PropertyIndexer providePropertyIndexer(final Workflows workflow, final Injector injector) {
        final Options options = getOptions(injector);
        return switch (options.caching) {
            case ENABLED -> new CachingPropertyIndexerImpl(options.mainCacheConfig, options.tmpCacheConfig);
            case DISABLED -> new PropertyIndexerImpl();
            case AUTO -> switch (workflow) {
                case deployment, vulcanizing -> new CachingPropertyIndexerImpl(options.mainCacheConfig, options.tmpCacheConfig);
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
                case development -> new CachingPropertyIndexerImpl(options.mainCacheConfig, options.tmpCacheConfig);
            };
        };
    }

    public static final class CacheConfig {
        public static final CacheConfig EMPTY = new CacheConfig();

        public final OptionalInt concurrencyLevel;
        public final OptionalLong maxSize;
        public final Optional<Duration> expireAfterAccess;
        public final Optional<Duration> expireAfterWrite;

        private CacheConfig(final OptionalInt concurrencyLevel, final OptionalLong maxSize,
                            final Optional<Duration> expireAfterAccess, final Optional<Duration> expireAfterWrite) {
            this.concurrencyLevel = concurrencyLevel;
            this.maxSize = maxSize;
            this.expireAfterAccess = expireAfterAccess;
            this.expireAfterWrite = expireAfterWrite;
        }

        private CacheConfig() {
            this(OptionalInt.empty(), OptionalLong.empty(), Optional.empty(), Optional.empty());
        }

        public CacheConfig concurrencyLevel(final int value) {
            return new CacheConfig(OptionalInt.of(value), maxSize, expireAfterAccess, expireAfterWrite);
        }

        public CacheConfig maxSize(final long value) {
            return new CacheConfig(concurrencyLevel, OptionalLong.of(value), expireAfterAccess, expireAfterWrite);
        }

        public CacheConfig expireAfterAccess(final Duration duration) {
            return new CacheConfig(concurrencyLevel, maxSize, Optional.of(duration), expireAfterWrite);
        }

        public CacheConfig expireAfterWrite(final Duration duration) {
            return new CacheConfig(concurrencyLevel, maxSize, expireAfterAccess, Optional.of(duration));
        }

        public static CacheConfig fromProperties(final Properties properties, final String prefix) {
            return Arrays.stream(CacheOptions.values()).map(opt -> {
                final String propName = prefix + "." + opt.name();
                return optPropertyParser(propName, opt.parser).apply(properties).getOrThrow();
            }).flatMap(Optional::stream).reduce(EMPTY, (cfg, fn) -> fn.apply(cfg),
                                                // no combiner
                                                ($1, $2) -> {throw new UnsupportedOperationException();});
        }

        /**
         * Merges cache configurations preferring present values from the right one.
         */
        public static CacheConfig rightMerge(final CacheConfig left, final CacheConfig right) {
            return new CacheConfig(
                    right.concurrencyLevel.isPresent() ? right.concurrencyLevel : left.concurrencyLevel,
                    right.maxSize.isPresent() ? right.maxSize : left.maxSize,
                    right.expireAfterAccess.isPresent() ? right.expireAfterAccess : left.expireAfterAccess,
                    right.expireAfterWrite.isPresent() ? right.expireAfterWrite : left.expireAfterWrite);
        }
    }

    /**
     * Parses a duration according to the following format: a non-negative decimal followed by a
     * {@linkplain DurationParser#UNITS unit character}.
     */
    private static final class DurationParser implements ValueParser<Object, Duration> {
        @Override
        public Result<Duration> apply(final Object value) {
            final var str = requireNonNull(value).toString();
            final int unitIdx = StringUtils.indexOfAny(str, UNITS);
            if (unitIdx <= 0) {
                return incorrectFormat(str);
            }

            final long number;
            try {
                number = Long.parseLong(str.substring(0, unitIdx));
            } catch (final NumberFormatException e) {
                return incorrectFormat(str, e);
            }

            final Duration duration;
            try {
                final char unit = str.charAt(unitIdx);
                duration = switch (unit) {
                    case 's' -> Duration.ofSeconds(number);
                    case 'm' -> Duration.ofMinutes(number);
                    case 'h' -> Duration.ofHours(number);
                    case 'd' -> Duration.ofDays(number);
                    default -> throw new InvalidArgumentException("Invalid unit: %s".formatted(unit));
                };
            } catch (final RuntimeException e) {
                return incorrectFormat(str, e);
            }

            return ok(duration);
        }

        private static final char[] UNITS = {
                's', // seconds
                'm', // minutes
                'h', // hours
                'd', // days
        };

        private static <T> Result<T> incorrectFormat(final String str) {
            return Result.error(format("Incorrect duration: '%s'. Expected a decimal followed by a unit (%s)",
                                       str, Arrays.toString(UNITS)));
        }

        private static <T> Result<T> incorrectFormat(final String str, final Throwable cause) {
            return Result.error(format("Incorrect duration: '%s'. Expected a decimal followed by a unit (%s)",
                                       str, Arrays.toString(UNITS)),
                                cause);
        }
    }

}
