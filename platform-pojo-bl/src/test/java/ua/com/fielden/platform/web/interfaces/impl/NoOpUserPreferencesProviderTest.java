package ua.com.fielden.platform.web.interfaces.impl;

import org.junit.Test;
import ua.com.fielden.platform.security.user.User;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class NoOpUserPreferencesProviderTest {

    private final NoOpUserPreferencesProvider provider = new NoOpUserPreferencesProvider();

    @Test
    public void returns_empty_map_for_null_user() {
        assertTrue(provider.getPreferencesFor(null).isEmpty());
    }

    @Test
    public void returns_empty_map_for_non_null_user() {
        assertTrue(provider.getPreferencesFor(new User()).isEmpty());
    }

    @Test
    public void returned_map_is_unmodifiable() {
        assertThrows(UnsupportedOperationException.class, () -> provider.getPreferencesFor(null).put("key", "value"));
    }

}