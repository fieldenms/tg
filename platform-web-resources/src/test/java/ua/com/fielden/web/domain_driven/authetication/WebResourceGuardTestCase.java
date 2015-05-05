package ua.com.fielden.web.domain_driven.authetication;

import static java.lang.String.format;
import static org.junit.Assert.*;

import java.security.SignatureException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CookieSetting;
import org.restlet.data.Method;
import org.restlet.data.Protocol;

import ua.com.fielden.platform.sample.domain.TgPerson;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.session.UserSessionDao;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.web.security.WebResourceGuard;
import ua.com.fielden.platform.web.test.IWebDrivenTestCaseConfiguration;
import ua.com.fielden.platform.web.test.WebBasedTestCase;

/**
 * A test case to ensure correct HTTP responses (HTTP codes and cookies) to requests for accessing guarded web resources.
 *
 * @author TG Team
 *
 */
public class WebResourceGuardTestCase extends AbstractDaoTestCase {

    private final UserSessionDao coSession = (UserSessionDao) ao(UserSession.class);
    private final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
    private final WebResourceGuardTestWebApplication webApp = getInstance(WebResourceGuardTestWebApplication.class);
    private final String baseUri = format("http://localhost:%s/v1", IWebDrivenTestCaseConfiguration.PORT);
    private final Client client = new Client(Protocol.HTTP);
    private IUser coUser = ao(User.class);

    @Before
    public void startUp() {
        coSession.getCache().invalidateAll();
        webApp.setCurrUser(coUser.findByKey("TEST"));
        WebBasedTestCase.attachWebApplication("/v1", webApp);
    }

    @After
    public void tearDown() {
        WebBasedTestCase.detachWebApplication(webApp);
    }

    @Test
    public void unauthenticated_request_should_be_refused_with_code_403_forbidden() throws SignatureException {
        final Request request = new Request(Method.GET, format("%s/users/TEST/%s/%s", baseUri, TgPerson.class.getSimpleName(), 12L));
        final Response response = client.handle(request);

        assertEquals(403, response.getStatus().getCode());
    }

    @Test
    public void authenticated_requests_to_existing_resources_from_just_logged_in_client_should_be_responded_with_code_200_ok() {
        constants.setNow(dateTime("2015-04-23 17:26:00"));
        // establish a new session
        final User currUser = getInstance(IUserProvider.class).getUser();
        final UserSession session = coSession.newSession(currUser, true);
        final String authenticator = session.getAuthenticator().get().toString();

        final Request request = new Request(Method.GET, format("%s/users/TEST/%s/%s", baseUri, TgPerson.class.getSimpleName(), 12L));
        final CookieSetting newCookie = new CookieSetting(1, WebResourceGuard.AUTHENTICATOR_COOKIE_NAME, authenticator, "/", null);
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
        final UserSession session = coSession.newSession(currUser, true);
        final String authenticator = session.getAuthenticator().get().toString();

        final Request request = new Request(Method.GET, format("%s/users/TEST/%s/%s", baseUri, TgPerson.class.getSimpleName(), 12L));
        final CookieSetting newCookie = new CookieSetting(1, WebResourceGuard.AUTHENTICATOR_COOKIE_NAME, authenticator, "/", null);
        newCookie.setAccessRestricted(true);
        request.getCookies().add(newCookie);

        final Response response = client.handle(request);

        assertEquals(200, response.getStatus().getCode());
        final CookieSetting returnedCookie = response.getCookieSettings().getFirst(WebResourceGuard.AUTHENTICATOR_COOKIE_NAME);
        assertNotNull("Authenticator cookie is expected.", returnedCookie);
        assertEquals("Returned authenticator should match the sent one due to very short time past since login.", authenticator, returnedCookie.getValue());
        assertTrue("Returned cookie must have restricted access.", returnedCookie.isAccessRestricted());
    }

    @Test
    public void authenticated_burst_requests_with_a_split_second_from_each_other_should_be_supported_for_trusted_devices() {
        constants.setNow(dateTime("2015-04-23 17:26:00"));
        // establish a new session
        final User currUser = getInstance(IUserProvider.class).getUser();
        final UserSession session = coSession.newSession(currUser, true);
        final String authenticator = session.getAuthenticator().get().toString();

        // some time passes by sufficient to evict authenticators from cache
        // the authenticator is still valid, but not in cache, which should lead to its regeneration
        // meanwhile, requests that followed the first one triggering re-authentication, should also be accepted...
        constants.setNow(dateTime("2015-04-23 18:26:00"));
        final Request request1 = new Request(Method.GET, format("%s/users/TEST/%s/%s", baseUri, TgPerson.class.getSimpleName(), 12L));
        final CookieSetting cookie = new CookieSetting(1, WebResourceGuard.AUTHENTICATOR_COOKIE_NAME, authenticator, "/", null);
        cookie.setAccessRestricted(true);
        request1.getCookies().add(cookie);

        final Request request2 = new Request(Method.GET, format("%s/users/TEST/%s/%s", baseUri, TgPerson.class.getSimpleName(), 12L));
        request2.getCookies().add(cookie);

        final Response response1 = client.handle(request1);
        constants.setNow(dateTime("2015-04-23 18:26:01"));
        final Response response2 = client.handle(request1);

        assertEquals(200, response1.getStatus().getCode());
        final CookieSetting returnedCookie1 = response1.getCookieSettings().getFirst(WebResourceGuard.AUTHENTICATOR_COOKIE_NAME);
        assertNotNull("Authenticator cookie is expected.", returnedCookie1);
        final String newAuthenticator = returnedCookie1.getValue();
        assertNotEquals("Returned authenticator should not match the sent one due to expected re-authentication.", authenticator, newAuthenticator);
        assertTrue("Returned cookie must have restricted access.", returnedCookie1.isAccessRestricted());

        assertEquals(200, response2.getStatus().getCode());
        final CookieSetting returnedCookie2 = response2.getCookieSettings().getFirst(WebResourceGuard.AUTHENTICATOR_COOKIE_NAME);
        assertNotNull("Authenticator cookie is expected.", returnedCookie2);
        assertEquals("Returned authenticator should match the re-generated one.", newAuthenticator, returnedCookie2.getValue());
        assertTrue("Returned cookie must have restricted access.", returnedCookie2.isAccessRestricted());
    }

    @Test
    public void authenticated_burst_requests_with_a_split_second_from_each_other_should_be_supported_for_untrusted_devices() {
        constants.setNow(dateTime("2015-04-23 17:26:00"));
        // establish a new session
        final User currUser = getInstance(IUserProvider.class).getUser();
        final UserSession session = coSession.newSession(currUser, false);
        final String authenticator = session.getAuthenticator().get().toString();

        // some time passes by sufficient to evict authenticators from cache, but insufficient to invalidate the original authenticator from an untrusted device
        // therefore, the authenticator is still valid, but not in cache, which should lead to its regeneration
        // meanwhile, requests that followed the first one triggering re-authentication, should also be accepted...
        constants.setNow(dateTime("2015-04-23 17:30:00"));
        final Request request1 = new Request(Method.GET, format("%s/users/TEST/%s/%s", baseUri, TgPerson.class.getSimpleName(), 12L));
        final CookieSetting cookie = new CookieSetting(1, WebResourceGuard.AUTHENTICATOR_COOKIE_NAME, authenticator, "/", null);
        cookie.setAccessRestricted(true);
        request1.getCookies().add(cookie);

        final Request request2 = new Request(Method.GET, format("%s/users/TEST/%s/%s", baseUri, TgPerson.class.getSimpleName(), 12L));
        request2.getCookies().add(cookie);

        final Response response1 = client.handle(request1);
        constants.setNow(dateTime("2015-04-23 17:30:01"));
        final Response response2 = client.handle(request1);

        assertEquals(200, response1.getStatus().getCode());
        final CookieSetting returnedCookie1 = response1.getCookieSettings().getFirst(WebResourceGuard.AUTHENTICATOR_COOKIE_NAME);
        assertNotNull("Authenticator cookie is expected.", returnedCookie1);
        final String newAuthenticator = returnedCookie1.getValue();
        assertNotEquals("Returned authenticator should not match the sent one due to expected re-authentication.", authenticator, newAuthenticator);
        assertTrue("Returned cookie must have restricted access.", returnedCookie1.isAccessRestricted());

        assertEquals(200, response2.getStatus().getCode());
        final CookieSetting returnedCookie2 = response2.getCookieSettings().getFirst(WebResourceGuard.AUTHENTICATOR_COOKIE_NAME);
        assertNotNull("Authenticator cookie is expected.", returnedCookie2);
        assertEquals("Returned authenticator should match the re-generated one.", newAuthenticator, returnedCookie2.getValue());
        assertTrue("Returned cookie must have restricted access.", returnedCookie2.isAccessRestricted());
    }


    @Test
    public void requests_with_expired_authenticators_from_untrusted_devices_should_respond_with_code_403_forbidden() {
        constants.setNow(dateTime("2015-04-23 17:26:00"));
        // establish a new session
        final User currUser = getInstance(IUserProvider.class).getUser();
        final UserSession session = coSession.newSession(currUser, false);
        final String authenticator = session.getAuthenticator().get().toString();

        // sufficient time passes by to invalidate the authenticator
        constants.setNow(dateTime("2015-04-23 17:33:00"));
        final Request request = new Request(Method.GET, format("%s/users/TEST/%s/%s", baseUri, TgPerson.class.getSimpleName(), 12L));
        final CookieSetting newCookie = new CookieSetting(1, WebResourceGuard.AUTHENTICATOR_COOKIE_NAME, authenticator, "/", null);
        newCookie.setAccessRestricted(true);
        request.getCookies().add(newCookie);

        final Response response = client.handle(request);

        assertEquals(403, response.getStatus().getCode());
    }

    @Test
    public void stolen_authenticator_from_trusted_device_should_be_recognised() {
        constants.setNow(dateTime("2015-04-23 17:26:00"));
        // establish a new session
        final User currUser = getInstance(IUserProvider.class).getUser();
        final UserSession session = coSession.newSession(currUser, true);
        final String stolenAuthenticator = session.getAuthenticator().get().toString();

        // an authenticator gets stolen and used by an adversary after sufficiently long time to evict authenticators from cache has passed
        // the authenticator is still valid, but not in cache, which should lead to its regeneration
        // any subsequent requests with the stolen authenticator post the cache eviction time after regeneration should lead to a recognition of the authenticator theft
        constants.setNow(dateTime("2015-04-23 18:26:00"));
        final Request request1 = new Request(Method.GET, format("%s/users/TEST/%s/%s", baseUri, TgPerson.class.getSimpleName(), 12L));
        final CookieSetting cookie = new CookieSetting(1, WebResourceGuard.AUTHENTICATOR_COOKIE_NAME, stolenAuthenticator, "/", null);
        cookie.setAccessRestricted(true);
        request1.getCookies().add(cookie);

        // request by an adversary
        final Response response1 = client.handle(request1);
        assertEquals(200, response1.getStatus().getCode());
        final CookieSetting returnedCookie1 = response1.getCookieSettings().getFirst(WebResourceGuard.AUTHENTICATOR_COOKIE_NAME);
        assertNotNull("Authenticator cookie is expected.", returnedCookie1);
        final String newAuthenticator = returnedCookie1.getValue();
        assertNotEquals("Returned authenticator should not match the sent one due to expected re-authentication.", stolenAuthenticator, newAuthenticator);
        assertTrue("Returned cookie must have restricted access.", returnedCookie1.isAccessRestricted());

        // request by a legitimate user with the stolen authenticator
        constants.setNow(dateTime("2015-04-23 18:40:00"));
        final Request request2 = new Request(Method.GET, format("%s/users/TEST/%s/%s", baseUri, TgPerson.class.getSimpleName(), 12L));
        request2.getCookies().add(cookie);
        final Response response2 = client.handle(request2);
        assertEquals(403, response2.getStatus().getCode());

        // another request by an adversary with what is thought a valid authenticator
        constants.setNow(dateTime("2015-04-23 18:41:00"));
        final Request request3 = new Request(Method.GET, format("%s/users/TEST/%s/%s", baseUri, TgPerson.class.getSimpleName(), 12L));
        request3.getCookies().add(returnedCookie1);
        final Response response3 = client.handle(request3);
        assertEquals(403, response3.getStatus().getCode());
    }
}