package ua.com.fielden.platform.web.security;

import org.junit.Test;
import org.restlet.engine.header.CookieSettingWriter;

import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.web.security.AbstractWebResourceGuard.mkAuthenticationCookie;
import static ua.com.fielden.platform.web.security.AbstractWebResourceGuard.mkAuthenticationCookieToExpire;

/// Tests for the `Set-Cookie` header that Restlet produces for the authenticating cookie.
///
/// Attribute `SameSite` is carried by the cookie path (see [AbstractWebResourceGuard#mkAuthenticationCookie]).
/// These tests verify the rendered header, so that a change in Restlet's `CookieSettingWriter` is detected.
///
public class AbstractWebResourceGuardCookieTest {

    private static void assertHeader(final String header, final String expectedPrefix, final String expectedSuffix) {
        assertTrue("Unexpected header start: " + header, header.startsWith(expectedPrefix));
        assertTrue("Unexpected header end: " + header, header.endsWith(expectedSuffix));
    }

    @Test
    public void authenticating_cookie_is_SameSite_Lax_Secure_and_HttpOnly() {
        final String header = CookieSettingWriter.write(mkAuthenticationCookie("user::authenticator", "tgdev.com", "/", 3600));
        assertHeader(header, "authenticator=user::authenticator; Path=/; SameSite=Lax; Expires=", "; Domain=tgdev.com; Secure; HttpOnly");
    }

    @Test
    public void expiring_authenticating_cookie_is_SameSite_Lax_Secure_and_HttpOnly() {
        final String header = CookieSettingWriter.write(mkAuthenticationCookieToExpire("tgdev.com", "/"));
        assertHeader(header, "authenticator=; Path=/; SameSite=Lax; Expires=", "; Domain=tgdev.com; Secure; HttpOnly");
    }

}
