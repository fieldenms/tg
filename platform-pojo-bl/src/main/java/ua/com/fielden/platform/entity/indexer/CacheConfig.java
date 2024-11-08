package ua.com.fielden.platform.entity.indexer;

import ua.com.fielden.platform.ioc.DynamicPropertyAccessIocModule;
import ua.com.fielden.platform.parser.DurationParser;
import ua.com.fielden.platform.parser.IValueParser;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;

import static ua.com.fielden.platform.parser.IValueParser.*;
import static ua.com.fielden.platform.parser.IValueParser.Result.ok;

/**
 * A cache configuration used by {@link CachingPropertyIndexerImpl}.
 */
public final class CacheConfig {
    public static final CacheConfig EMPTY = new CacheConfig();

    public final OptionalInt concurrencyLevel;
    public final OptionalLong maxSize;
    public final Optional<Duration> expireAfterAccess;
    public final Optional<Duration> expireAfterWrite;

    public enum CacheOptions {

        concurrencyLevel(intParser().and(i -> ok(cfg -> cfg.concurrencyLevel(i)))),
        maxSize(longParser().and(l -> ok(cfg -> cfg.maxSize(l)))),
        expireAfterAccess(new DurationParser().and(duration -> ok(cfg -> cfg.expireAfterAccess(duration)))),
        expireAfterWrite(new DurationParser().and(duration -> ok(cfg -> cfg.expireAfterWrite(duration))));

        public final IValueParser<Object, Function<CacheConfig, CacheConfig>> parser;

        CacheOptions(final IValueParser<Object, Function<CacheConfig, CacheConfig>> parser) {
            this.parser = parser;
        }
    }

    private CacheConfig(
            final OptionalInt concurrencyLevel,
            final OptionalLong maxSize,
            final Optional<Duration> expireAfterAccess,
            final Optional<Duration> expireAfterWrite) {
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
                ($1, $2) -> {
                    throw new UnsupportedOperationException();
                });
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
