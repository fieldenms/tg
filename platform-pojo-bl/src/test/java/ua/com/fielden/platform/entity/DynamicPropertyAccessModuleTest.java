package ua.com.fielden.platform.entity;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.junit.Test;
import ua.com.fielden.platform.parser.ValueParsingException;

import java.util.Map;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static ua.com.fielden.platform.entity.DynamicPropertyAccessModule.Options.CACHING_PROPERTY;
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

}
