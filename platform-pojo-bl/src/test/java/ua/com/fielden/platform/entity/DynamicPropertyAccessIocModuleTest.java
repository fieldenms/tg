package ua.com.fielden.platform.entity;

import org.junit.Test;
import ua.com.fielden.platform.parser.ValueParsingException;

import java.time.Duration;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static ua.com.fielden.platform.ioc.DynamicPropertyAccessIocModule.CacheOptions.expireAfterAccess;
import static ua.com.fielden.platform.ioc.DynamicPropertyAccessIocModule.CacheOptions.expireAfterWrite;
import static ua.com.fielden.platform.ioc.DynamicPropertyAccessIocModule.Options.*;
import static ua.com.fielden.platform.ioc.DynamicPropertyAccessIocModule.Options.Caching.*;
import static ua.com.fielden.platform.ioc.DynamicPropertyAccessIocModule.options;

public class DynamicPropertyAccessIocModuleTest {

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
                     () -> options().fromMap(Map.of(cachePropertyName(MAIN_CACHE_PROPERTY_PREFIX, expireAfterAccess), "s")));
        assertThrows(ValueParsingException.class,
                     () -> options().fromMap(Map.of(cachePropertyName(MAIN_CACHE_PROPERTY_PREFIX, expireAfterAccess), "1234")));
        assertEquals(Duration.ofDays(5),
                     options().fromMap(Map.of(cachePropertyName(MAIN_CACHE_PROPERTY_PREFIX, expireAfterAccess), "5d"))
                             .mainCacheConfig.expireAfterAccess.orElseThrow());
        assertEquals(Duration.ofHours(48),
                     options().fromMap(Map.of(cachePropertyName(MAIN_CACHE_PROPERTY_PREFIX, expireAfterAccess), "48h"))
                             .mainCacheConfig.expireAfterAccess.orElseThrow());
        assertEquals(Duration.ofMinutes(73),
                     options().fromMap(Map.of(cachePropertyName(MAIN_CACHE_PROPERTY_PREFIX, expireAfterAccess), "73m"))
                             .mainCacheConfig.expireAfterAccess.orElseThrow());
        assertEquals(Duration.ofSeconds(2),
                     options().fromMap(Map.of(cachePropertyName(MAIN_CACHE_PROPERTY_PREFIX, expireAfterAccess), "2s"))
                             .mainCacheConfig.expireAfterAccess.orElseThrow());
        assertEquals(Duration.ofSeconds(200000),
                     options().fromMap(Map.of(cachePropertyName(MAIN_CACHE_PROPERTY_PREFIX, expireAfterAccess), "200000s"))
                             .mainCacheConfig.expireAfterAccess.orElseThrow());
    }

    @Test
    public void expireAfterWrite_option_is_parsed_correctly() {
        assertThrows(ValueParsingException.class,
                     () -> options().fromMap(Map.of(cachePropertyName(MAIN_CACHE_PROPERTY_PREFIX, expireAfterWrite), "s")));
        assertThrows(ValueParsingException.class,
                     () -> options().fromMap(Map.of(cachePropertyName(MAIN_CACHE_PROPERTY_PREFIX, expireAfterWrite), "1234")));
        assertEquals(Duration.ofDays(5),
                     options().fromMap(Map.of(cachePropertyName(MAIN_CACHE_PROPERTY_PREFIX, expireAfterWrite), "5d"))
                             .mainCacheConfig.expireAfterWrite.orElseThrow());
        assertEquals(Duration.ofHours(48),
                     options().fromMap(Map.of(cachePropertyName(MAIN_CACHE_PROPERTY_PREFIX, expireAfterWrite), "48h"))
                             .mainCacheConfig.expireAfterWrite.orElseThrow());
        assertEquals(Duration.ofMinutes(73),
                     options().fromMap(Map.of(cachePropertyName(MAIN_CACHE_PROPERTY_PREFIX, expireAfterWrite), "73m"))
                             .mainCacheConfig.expireAfterWrite.orElseThrow());
        assertEquals(Duration.ofSeconds(2),
                     options().fromMap(Map.of(cachePropertyName(MAIN_CACHE_PROPERTY_PREFIX, expireAfterWrite), "2s"))
                             .mainCacheConfig.expireAfterWrite.orElseThrow());
        assertEquals(Duration.ofSeconds(200000),
                     options().fromMap(Map.of(cachePropertyName(MAIN_CACHE_PROPERTY_PREFIX, expireAfterWrite), "200000s"))
                             .mainCacheConfig.expireAfterWrite.orElseThrow());
    }

}
