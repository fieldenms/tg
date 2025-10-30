package ua.com.fielden.web.domain_driven.authetication;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CookieSetting;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import ua.com.fielden.platform.sample.domain.TgPerson;
import ua.com.fielden.platform.security.session.Authenticator;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.session.UserSessionDao;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.web.security.AbstractWebResourceGuard;
import ua.com.fielden.platform.web.test.TestWebApplication;

import java.security.SignatureException;
import java.util.Optional;

import static org.junit.Assert.*;

/// A test case to ensure correct HTTP responses (HTTP codes and cookies) to requests for accessing guarded web resources.
///
public class WebResourceGuardTestCase extends AbstractDaoTestCase {

    private final UserSessionDao coSession = co$(UserSession.class);
    private final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
    private final WebResourceGuardTestWebApplication webApp = getInstance(WebResourceGuardTestWebApplication.class);
    private final String baseUri = "http://localhost:%s/v1".formatted(TestWebApplication.PORT);
    private final Client client = new Client(new Context(), Protocol.HTTP);
    {
        // Redirects should not be followed, which became the default and needs to be turned off for testing.
        client.getContext().getParameters().add("followRedirects", "false");
    }
    private final IUser coUser = co$(User.class);

    @Before
    public void startUp() {
        coSession.getCache().invalidateAll();
        webApp.setCurrUser(coUser.findByKey(User.system_users.UNIT_TEST_USER.name()));
        TestWebApplication.attachWebApplication("/v1", webApp);
    }

    @After
    public void tearDown() {
        TestWebApplication.detachWebApplication(webApp);
    }

    @Test
    public void unauthenticated_POST_request_should_be_refused_with_code_403_forbidden() throws SignatureException {
        final Request request = new Request(Method.POST, "%s/users/%s/%s/%s".formatted(baseUri, User.system_users.UNIT_TEST_USER, TgPerson.class.getSimpleName(), 12L));
        final Response response = client.handle(request);

        assertEquals(403, response.getStatus().getCode());
    }

    @Test
    public void unauthenticated_PUT_request_should_be_refused_with_code_403_forbidden() throws SignatureException {
        final Request request = new Request(Method.POST, "%s/users/%s/%s/%s".formatted(baseUri, User.system_users.UNIT_TEST_USER, TgPerson.class.getSimpleName(), 12L));
        final Response response = client.handle(request);

        assertEquals(403, response.getStatus().getCode());
    }

    @Test
    public void unauthenticated_GET_request_should_be_redirected() throws SignatureException {
        final Request request = new Request(Method.GET, "%s/users/%s/%s/%s".formatted(baseUri, User.system_users.UNIT_TEST_USER, TgPerson.class.getSimpleName(), 12L));
        final Response response = client.handle(request);

        assertEquals(307, response.getStatus().getCode());
    }

    @Test
    public void authenticated_requests_to_existing_resources_from_just_logged_in_client_should_be_responded_with_code_200_ok() {
        constants.setNow(dateTime("2015-04-23 17:26:00"));
        // establish a new session
        final User currUser = getInstance(IUserProvider.class).getUser();
        final UserSession session = coSession.newSession(currUser, true, null);
        final String authenticator = session.getAuthenticator().map(Authenticator::toString).orElseThrow();

        final Request request = new Request(Method.GET, "%s/users/%s/%s/%s".formatted(baseUri, currUser.getKey(), TgPerson.class.getSimpleName(), 12L));
        final CookieSetting newCookie = new CookieSetting(1, AbstractWebResourceGuard.AUTHENTICATOR_COOKIE_NAME, authenticator, "/", null);
        newCookie.setAccessRestricted(true);
        request.getCookies().add(newCookie);

        final Response response = client.handle(request);

        assertEquals(200, response.getStatus().getCode());
    }

    @Test
    public void authenticated_requests_from_just_logged_in_client_should_be_responded_with_authenticator_cookie_matching_original_authenticator() {
        constants.setNow(dateTime("2015-04-23 17:26:00"));
        // establish a new session
        final User currUser = getInstance(IUserProvider.class).getUser();
        final UserSession session = coSession.newSession(currUser, true, null);
        final String authenticator = session.getAuthenticator().map(Authenticator::toString).orElseThrow();

        final Request request = new Request(Method.GET, "%s/users/%s/%s/%s".formatted(baseUri, User.system_users.UNIT_TEST_USER, TgPerson.class.getSimpleName(), 12L));
        final CookieSetting newCookie = new CookieSetting(1, AbstractWebResourceGuard.AUTHENTICATOR_COOKIE_NAME, authenticator, "/", null);
        newCookie.setAccessRestricted(true);
        request.getCookies().add(newCookie);

        final Response response = client.handle(request);

        assertEquals(200, response.getStatus().getCode());
        final CookieSetting returnedCookie = response.getCookieSettings().getFirst(AbstractWebResourceGuard.AUTHENTICATOR_COOKIE_NAME);
        assertNotNull("Authenticator cookie is expected.", returnedCookie);
        assertEquals("Returned authenticator should match the sent one due to very short time past since login.", authenticator, returnedCookie.getValue());
        assertTrue("Returned cookie must have restricted access.", returnedCookie.isAccessRestricted());
    }

    @Test
    public void authenticated_burst_requests_with_a_split_second_from_each_other_should_be_supported_for_trusted_devices() {
        constants.setNow(dateTime("2015-04-23 17:26:00"));
        // establish a new session
        final User currUser = getInstance(IUserProvider.class).getUser();
        final UserSession session = coSession.newSession(currUser, true, null);
        final String authenticator = session.getAuthenticator().map(Authenticator::toString).orElseThrow();

        // some time passes by sufficient to evict authenticators from cache
        // the authenticator is still valid, but not in cache, which should lead to its regeneration
        // meanwhile, requests that followed the first one triggering re-authentication, should also be accepted...
        constants.setNow(dateTime("2015-04-23 18:26:00"));
        final Request request1 = new Request(Method.GET, "%s/users/%s/%s/%s".formatted(baseUri, User.system_users.UNIT_TEST_USER, TgPerson.class.getSimpleName(), 12L));
        final CookieSetting cookie = new CookieSetting(1, AbstractWebResourceGuard.AUTHENTICATOR_COOKIE_NAME, authenticator, "/", null);
        cookie.setAccessRestricted(true);
        request1.getCookies().add(cookie);

        final Request request2 = new Request(Method.GET, "%s/users/%s/%s/%s".formatted(baseUri, User.system_users.UNIT_TEST_USER, TgPerson.class.getSimpleName(), 12L));
        request2.getCookies().add(cookie);

        final Response response1 = client.handle(request1);
        constants.setNow(dateTime("2015-04-23 18:26:01"));
        final Response response2 = client.handle(request1);

        assertEquals(200, response1.getStatus().getCode());
        final CookieSetting returnedCookie1 = response1.getCookieSettings().getFirst(AbstractWebResourceGuard.AUTHENTICATOR_COOKIE_NAME);
        assertNotNull("Authenticator cookie is expected.", returnedCookie1);
        final String newAuthenticator = returnedCookie1.getValue();
        assertNotEquals("Returned authenticator should not match the sent one due to expected re-authentication.", authenticator, newAuthenticator);
        assertTrue("Returned cookie must have restricted access.", returnedCookie1.isAccessRestricted());

        assertEquals(200, response2.getStatus().getCode());
        final CookieSetting returnedCookie2 = response2.getCookieSettings().getFirst(AbstractWebResourceGuard.AUTHENTICATOR_COOKIE_NAME);
        assertNotNull("Authenticator cookie is expected.", returnedCookie2);
        assertEquals("Returned authenticator should match the re-generated one.", newAuthenticator, returnedCookie2.getValue());
        assertTrue("Returned cookie must have restricted access.", returnedCookie2.isAccessRestricted());
    }

    @Test
    public void authenticated_burst_requests_with_a_split_second_from_each_other_should_be_supported_for_untrusted_devices() {
        constants.setNow(dateTime("2015-04-23 17:26:00"));
        // establish a new session
        final User currUser = getInstance(IUserProvider.class).getUser();
        final UserSession session = coSession.newSession(currUser, false, null);
        final String authenticator = session.getAuthenticator().map(Authenticator::toString).orElseThrow();

        // some time passes by sufficient to evict authenticators from cache, but insufficient to invalidate the original authenticator from an untrusted device
        // therefore, the authenticator is still valid, but not in cache, which should lead to its regeneration
        // meanwhile, requests that followed the first one triggering re-authentication, should also be accepted...
        constants.setNow(dateTime("2015-04-23 17:30:00"));
        final Request request1 = new Request(Method.GET, "%s/users/%s/%s/%s".formatted(baseUri, User.system_users.UNIT_TEST_USER, TgPerson.class.getSimpleName(), 12L));
        final CookieSetting cookie = new CookieSetting(1, AbstractWebResourceGuard.AUTHENTICATOR_COOKIE_NAME, authenticator, "/", null);
        cookie.setAccessRestricted(true);
        request1.getCookies().add(cookie);

        final Request request2 = new Request(Method.GET, "%s/users/%s/%s/%s".formatted(baseUri, User.system_users.UNIT_TEST_USER, TgPerson.class.getSimpleName(), 12L));
        request2.getCookies().add(cookie);

        final Response response1 = client.handle(request1);
        constants.setNow(dateTime("2015-04-23 17:30:01"));
        final Response response2 = client.handle(request1);

        assertEquals(200, response1.getStatus().getCode());
        final CookieSetting returnedCookie1 = response1.getCookieSettings().getFirst(AbstractWebResourceGuard.AUTHENTICATOR_COOKIE_NAME);
        assertNotNull("Authenticator cookie is expected.", returnedCookie1);
        final String newAuthenticator = returnedCookie1.getValue();
        assertNotEquals("Returned authenticator should not match the sent one due to expected re-authentication.", authenticator, newAuthenticator);
        assertTrue("Returned cookie must have restricted access.", returnedCookie1.isAccessRestricted());

        assertEquals(200, response2.getStatus().getCode());
        final CookieSetting returnedCookie2 = response2.getCookieSettings().getFirst(AbstractWebResourceGuard.AUTHENTICATOR_COOKIE_NAME);
        assertNotNull("Authenticator cookie is expected.", returnedCookie2);
        assertEquals("Returned authenticator should match the re-generated one.", newAuthenticator, returnedCookie2.getValue());
        assertTrue("Returned cookie must have restricted access.", returnedCookie2.isAccessRestricted());
    }


    @Test
    public void POST_requests_with_expired_authenticators_from_untrusted_devices_should_respond_with_code_403_forbidden() {
        constants.setNow(dateTime("2015-04-23 17:26:00"));
        // establish a new session
        final User currUser = getInstance(IUserProvider.class).getUser();
        final UserSession session = coSession.newSession(currUser, false, null);
        final String authenticator = session.getAuthenticator().map(Authenticator::toString).orElseThrow();

        // sufficient time passes by to invalidate the authenticator
        constants.setNow(dateTime("2015-04-23 17:33:00"));
        final Request request = new Request(Method.POST, "%s/users/%s/%s/%s".formatted(baseUri, User.system_users.UNIT_TEST_USER, TgPerson.class.getSimpleName(), 12L));
        final CookieSetting newCookie = new CookieSetting(1, AbstractWebResourceGuard.AUTHENTICATOR_COOKIE_NAME, authenticator, "/", null);
        newCookie.setAccessRestricted(true);
        request.getCookies().add(newCookie);

        final Response response = client.handle(request);

        assertEquals(403, response.getStatus().getCode());
    }

    @Test
    public void GET_requests_with_expired_authenticators_from_untrusted_devices_should_respond_with_code_305_redirect() {
        constants.setNow(dateTime("2015-04-23 17:26:00"));
        // establish a new session
        final User currUser = getInstance(IUserProvider.class).getUser();
        final UserSession session = coSession.newSession(currUser, false, null);
        final String authenticator = session.getAuthenticator().map(Authenticator::toString).orElseThrow();

        // sufficient time passes by to invalidate the authenticator
        constants.setNow(dateTime("2015-04-23 17:33:00"));
        final Request request = new Request(Method.GET, "%s/users/%s/%s/%s".formatted(baseUri, User.system_users.UNIT_TEST_USER, TgPerson.class.getSimpleName(), 12L));
        final CookieSetting newCookie = new CookieSetting(1, AbstractWebResourceGuard.AUTHENTICATOR_COOKIE_NAME, authenticator, "/", null);
        newCookie.setAccessRestricted(true);
        request.getCookies().add(newCookie);

        final Response response = client.handle(request);

        assertEquals(307, response.getStatus().getCode());
    }

    @Test
    public void stolen_authenticator_from_trusted_device_should_be_recognised() {
        constants.setNow(dateTime("2015-04-23 17:26:00"));
        // Establish a new session.
        final User currUser = getInstance(IUserProvider.class).getUser();
        final UserSession session = coSession.newSession(currUser, true, null);
        final String stolenAuthenticator = session.getAuthenticator().map(Authenticator::toString).orElseThrow();

        // An authenticator gets stolen and used by an adversary after a sufficiently long time to evict authenticators from cache has passed.
        // The authenticator is still valid, but not in cache, which should lead to its regeneration.
        // Any subsequent requests with the stolen authenticator post the eviction time after regeneration should lead to a recognition of that authenticator as invalid
        constants.setNow(dateTime("2015-04-23 18:26:00"));
        final Request request1 = new Request(Method.GET, "%s/users/%s/%s/%s".formatted(baseUri, User.system_users.UNIT_TEST_USER, TgPerson.class.getSimpleName(), 12L));
        final CookieSetting cookie = new CookieSetting(1, AbstractWebResourceGuard.AUTHENTICATOR_COOKIE_NAME, stolenAuthenticator, "/", null);
        cookie.setAccessRestricted(true);
        request1.getCookies().add(cookie);

        // Request by a user.
        final Response response1 = client.handle(request1);
        assertEquals(200, response1.getStatus().getCode());
        final CookieSetting returnedCookie1 = response1.getCookieSettings().getFirst(AbstractWebResourceGuard.AUTHENTICATOR_COOKIE_NAME);
        assertNotNull("Authenticator cookie is expected.", returnedCookie1);
        final String newAuthenticator = returnedCookie1.getValue();
        assertNotEquals("Returned authenticator should not match the sent one due to expected re-authentication.", stolenAuthenticator, newAuthenticator);
        assertTrue("Returned cookie must have restricted access.", returnedCookie1.isAccessRestricted());

        // request by an adversary with the stolen authenticator
        constants.setNow(dateTime("2015-04-23 18:40:00"));
        final Request request2 = new Request(Method.GET, "%s/users/%s/%s/%s".formatted(baseUri, User.system_users.UNIT_TEST_USER, TgPerson.class.getSimpleName(), 12L));
        request2.getCookies().add(cookie);
        final Response response2 = client.handle(request2);
        assertEquals(307, response2.getStatus().getCode());
        assertEquals(1, response2.getCookieSettings().size());
        final CookieSetting authCookie = response2.getCookieSettings().getFirst();
        assertEquals("", authCookie.getValue());
        assertEquals(0, authCookie.getMaxAge());
    }

    @Test
    public void extractAuthenticator_returns_the_latest_authenticator_if_multiple_authentication_cookies_are_present() {
        constants.setNow(dateTime("2015-04-23 17:26:00"));
        final User currUser = getInstance(IUserProvider.class).getUser();

        // generate two authenticators with the second authenticator having a greater expiration time
        final Authenticator auth1 = coSession.mkAuthenticator(currUser, coSession.genSeriesId(), 1L, dateTime("2015-04-23 19:00:01").toDate());
        final Authenticator auth2 = coSession.mkAuthenticator(currUser, coSession.genSeriesId(), 2L, dateTime("2015-04-23 19:00:00").toDate());
        final String strAuth1 = auth1.toString();
        final String strAuth2 = auth2.toString();

        final Request request = new Request(Method.GET, "%s/users/%s/%s/%s".formatted(baseUri, User.system_users.UNIT_TEST_USER, TgPerson.class.getSimpleName(), 12L));
        final CookieSetting cookie1 = new CookieSetting(1, AbstractWebResourceGuard.AUTHENTICATOR_COOKIE_NAME, strAuth1, "/", null);
        cookie1.setAccessRestricted(true);
        final CookieSetting cookie2 = new CookieSetting(1, AbstractWebResourceGuard.AUTHENTICATOR_COOKIE_NAME, strAuth2, "/", null);
        cookie2.setAccessRestricted(true);
        
        request.getCookies().add(cookie1);
        request.getCookies().add(cookie2);

        final Optional<Authenticator> auth = AbstractWebResourceGuard.extractAuthenticator(request);
        assertEquals(strAuth2, auth.map(Authenticator::toString).orElseThrow());
    }

}
