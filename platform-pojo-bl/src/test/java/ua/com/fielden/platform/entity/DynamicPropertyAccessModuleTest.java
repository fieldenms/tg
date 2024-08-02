package ua.com.fielden.platform.entity;

import org.junit.Test;
import ua.com.fielden.platform.parser.ValueParsingException;

import java.time.Duration;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static ua.com.fielden.platform.entity.DynamicPropertyAccessModule.CacheOptions.expireAfterAccess;
import static ua.com.fielden.platform.entity.DynamicPropertyAccessModule.CacheOptions.expireAfterWrite;
import static ua.com.fielden.platform.entity.DynamicPropertyAccessModule.Options.*;
import static ua.com.fielden.platform.entity.DynamicPropertyAccessModule.Options.Caching.*;
import static ua.com.fielden.platform.entity.DynamicPropertyAccessModule.options;

public class DynamicPropertyAccessModuleTest {

    @Test
    public void caching_option_is_parsed_correctly() {
        assertEquals(AUTO, options().fromMap(Map.of(CACHING_PROPERTY, "auto")).caching);
        assertEquals(AUTO, options().fromMap(Map.of(CACHING_PROPERTY, "AuTo")).caching);
        assertEquals(AUTO, options().fromMap(Map.of(CACHING_PROPERTY, "AUTO")).caching);

        assertEquals(ENABLED, options().fromMap(Map.of(CACHING_PROPERTY, "enabled")).caching);
        assertEquals(ENABLED, options().fromMap(Map.of(CACHING_PROPERTY, "ENabLED")).caching);

        assertEquals(DISABLED, options().fromMap(Map.of(CACHING_PROPERTY, "disabled")).caching);
        assertEquals(DISABLED, options().fromMap(Map.of(CACHING_PROPERTY, "DISABLED")).caching);

        assertThrows(ValueParsingException.class,
                     () -> options().fromMap(Map.of(CACHING_PROPERTY, "something else")));
    }

    @Test
    public void expireAfterAccess_option_is_parsed_correctly() {
        assertThrows(ValueParsingException.class,
                     () -> options().fromMap(Map.of(cachePropertyName(LASTING_CACHE_PROPERTY_PREFIX, expireAfterAccess), "s")));
        assertThrows(ValueParsingException.class,
                     () -> options().fromMap(Map.of(cachePropertyName(LASTING_CACHE_PROPERTY_PREFIX, expireAfterAccess), "1234")));
        assertEquals(Duration.ofDays(5),
                     options().fromMap(Map.of(cachePropertyName(LASTING_CACHE_PROPERTY_PREFIX, expireAfterAccess), "5d"))
                             .lastingCacheConfig.expireAfterAccess.orElseThrow());
        assertEquals(Duration.ofHours(48),
                     options().fromMap(Map.of(cachePropertyName(LASTING_CACHE_PROPERTY_PREFIX, expireAfterAccess), "48h"))
                             .lastingCacheConfig.expireAfterAccess.orElseThrow());
        assertEquals(Duration.ofMinutes(73),
                     options().fromMap(Map.of(cachePropertyName(LASTING_CACHE_PROPERTY_PREFIX, expireAfterAccess), "73m"))
                             .lastingCacheConfig.expireAfterAccess.orElseThrow());
        assertEquals(Duration.ofSeconds(2),
                     options().fromMap(Map.of(cachePropertyName(LASTING_CACHE_PROPERTY_PREFIX, expireAfterAccess), "2s"))
                             .lastingCacheConfig.expireAfterAccess.orElseThrow());
        assertEquals(Duration.ofSeconds(200000),
                     options().fromMap(Map.of(cachePropertyName(LASTING_CACHE_PROPERTY_PREFIX, expireAfterAccess), "200000s"))
                             .lastingCacheConfig.expireAfterAccess.orElseThrow());
    }

    @Test
    public void expireAfterWrite_option_is_parsed_correctly() {
        assertThrows(ValueParsingException.class,
                     () -> options().fromMap(Map.of(cachePropertyName(LASTING_CACHE_PROPERTY_PREFIX, expireAfterWrite), "s")));
        assertThrows(ValueParsingException.class,
                     () -> options().fromMap(Map.of(cachePropertyName(LASTING_CACHE_PROPERTY_PREFIX, expireAfterWrite), "1234")));
        assertEquals(Duration.ofDays(5),
                     options().fromMap(Map.of(cachePropertyName(LASTING_CACHE_PROPERTY_PREFIX, expireAfterWrite), "5d"))
                             .lastingCacheConfig.expireAfterWrite.orElseThrow());
        assertEquals(Duration.ofHours(48),
                     options().fromMap(Map.of(cachePropertyName(LASTING_CACHE_PROPERTY_PREFIX, expireAfterWrite), "48h"))
                             .lastingCacheConfig.expireAfterWrite.orElseThrow());
        assertEquals(Duration.ofMinutes(73),
                     options().fromMap(Map.of(cachePropertyName(LASTING_CACHE_PROPERTY_PREFIX, expireAfterWrite), "73m"))
                             .lastingCacheConfig.expireAfterWrite.orElseThrow());
        assertEquals(Duration.ofSeconds(2),
                     options().fromMap(Map.of(cachePropertyName(LASTING_CACHE_PROPERTY_PREFIX, expireAfterWrite), "2s"))
                             .lastingCacheConfig.expireAfterWrite.orElseThrow());
        assertEquals(Duration.ofSeconds(200000),
                     options().fromMap(Map.of(cachePropertyName(LASTING_CACHE_PROPERTY_PREFIX, expireAfterWrite), "200000s"))
                             .lastingCacheConfig.expireAfterWrite.orElseThrow());
    }

}
