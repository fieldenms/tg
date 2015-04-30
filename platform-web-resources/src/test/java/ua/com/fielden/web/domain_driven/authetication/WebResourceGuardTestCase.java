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
    public void authenticated_requests_from_just_logged_in_client_should_not_responded_with_code_403_forbidden() {
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
}