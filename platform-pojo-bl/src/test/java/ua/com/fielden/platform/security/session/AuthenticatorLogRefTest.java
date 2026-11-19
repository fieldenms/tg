package ua.com.fielden.platform.security.session;

import org.junit.Test;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.security.session.Authenticator.AUTHENTICATOR_SEPARATOR;

public class AuthenticatorLogRefTest {

    private static final String SERIES_ID = "5f2c1c7b9a3e4d6f8a0b1c2d3e4f5a6b";
    private static final String HASH = "9D8C7B6A5F4E3D2C1B0A99887766554433221100";

    private static Authenticator authenticator(final String username, final String seriesId, final long version, final String hash) {
        return new Authenticator(String.join(AUTHENTICATOR_SEPARATOR, username, seriesId, Long.toString(version)), hash);
    }

    @Test
    public void log_reference_names_the_user_and_discloses_neither_the_series_id_nor_the_hash() {
        final Authenticator auth = authenticator("USER1", SERIES_ID, 1L, HASH);
        final String ref = auth.logRef();

        assertTrue(ref.startsWith("USER1" + AUTHENTICATOR_SEPARATOR));
        assertFalse(ref.contains(SERIES_ID));
        assertFalse(ref.contains(HASH));
        // The value of the authenticator, on the other hand, contains both.
        assertTrue(auth.toString().contains(SERIES_ID));
        assertTrue(auth.toString().contains(HASH));
    }

    @Test
    public void log_reference_is_the_same_for_the_same_authenticator_and_differs_for_different_ones() {
        assertEquals(authenticator("USER1", SERIES_ID, 1L, HASH).logRef(), authenticator("USER1", SERIES_ID, 1L, HASH).logRef());
        assertNotEquals(authenticator("USER1", SERIES_ID, 1L, HASH).logRef(), authenticator("USER1", SERIES_ID, 2L, HASH).logRef());
        assertNotEquals(authenticator("USER1", SERIES_ID, 1L, HASH).logRef(), authenticator("USER1", SERIES_ID + "0", 1L, HASH).logRef());
    }

}
