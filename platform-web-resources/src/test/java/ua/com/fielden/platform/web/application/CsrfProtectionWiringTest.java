package ua.com.fielden.platform.web.application;

import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Cookie;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.routing.Router;
import ua.com.fielden.platform.security.session.Authenticator;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.session.UserSessionDao;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.resources.test.AbstractWebResourceWithDaoTestCase;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.web.filters.CsrfProtectionFilter.HEADER_ORIGIN;
import static ua.com.fielden.platform.web.filters.CsrfProtectionFilter.HEADER_SEC_FETCH_SITE;
import static ua.com.fielden.platform.web.security.AbstractWebResourceGuard.AUTHENTICATOR_COOKIE_NAME;

/// Tests that [AbstractWebUiResources] wires [ua.com.fielden.platform.web.filters.CsrfProtectionFilter] in front of the guarded resources.
///
/// Requests carry a valid session cookie, which the guard accepts, so that a rejection can only come from the filter.
/// The session cache is cleared before each request, so that the guard authenticates it fully and caches its authenticator.
/// An uncached authenticator after a request shows that the request did not reach the guard.
/// Which requests are forged is covered by [ua.com.fielden.platform.web.filters.CsrfProtectionFilterTest].
///
public class CsrfProtectionWiringTest extends AbstractWebResourceWithDaoTestCase {

    private static final String
            HOST = "https://tgdev.com",
            PROBE_PATH = "/csrf-probe";

    private final IUserProvider up = getInstance(IUserProvider.class);
    private final UserSessionDao coSession = co$(UserSession.class);

    /// A guarded resource that records the requests reaching it.
    ///
    private static final class Probe extends Restlet {
        private int requests;

        @Override
        public void handle(final Request request, final Response response) {
            requests++;
            response.setStatus(Status.SUCCESS_OK);
        }
    }

    /// An application that registers [Probe] as its only domain resource.
    ///
    private static final class ProbeWebUiResources extends AbstractWebUiResources {
        private final Probe probe;

        private ProbeWebUiResources(final Injector injector, final Probe probe) {
            super(new Context(), injector, "CSRF wiring test", "", "", "", injector.getInstance(IWebUiConfig.class));
            this.probe = probe;
        }

        @Override
        protected void registerDomainWebResources(final Router router, final IWebUiConfig webApp) {
            router.attach(PROBE_PATH, probe);
        }
    }

    private Probe probe;
    private Restlet root;
    /// The authenticator of the session cookie of the last handled request.
    private String authenticator;

    @Before
    public void setUp() {
        probe = new Probe();
        root = new ProbeWebUiResources(getInstance(Injector.class), probe).createInboundRoot();
    }

    /// Sends a request to the probe with a valid session cookie of the current user and `headers`.
    /// The session cache is cleared before the request, so that [#reachedTheGuard()] can tell whether the guard authenticated it.
    /// The application clears the current user once the request is handled, so it is restored afterwards.
    ///
    private Status sendRequest(final Method method, final String... headers) {
        final User currUser = up.getUser();
        authenticator = coSession.newSession(currUser, false, null).getAuthenticator().map(Authenticator::toString).orElseThrow();
        coSession.getCache().invalidateAll();

        final var request = new Request(method, HOST + PROBE_PATH);
        request.setHostRef(HOST);
        // Routing matches the part of the resource reference that remains after the base reference, which the virtual host sets otherwise.
        request.getResourceRef().setBaseRef(HOST);
        request.getCookies().add(new Cookie(AUTHENTICATOR_COOKIE_NAME, authenticator));
        for (int index = 0; index < headers.length; index += 2) {
            request.getHeaders().add(headers[index], headers[index + 1]);
        }
        final var response = new Response(request);
        try {
            root.handle(request, response);
        } finally {
            up.setUser(currUser);
        }
        return response.getStatus();
    }

    /// Checks whether the last handled request reached the guard, which caches the authenticator of an authenticated request.
    ///
    private boolean reachedTheGuard() {
        return coSession.getCache().getIfPresent(authenticator) != null;
    }

    @Test
    public void same_origin_POST_reaches_the_guarded_resource() {
        assertEquals(Status.SUCCESS_OK, sendRequest(Method.POST, HEADER_SEC_FETCH_SITE, "same-origin", HEADER_ORIGIN, HOST));
        assertTrue(reachedTheGuard());
        assertEquals(1, probe.requests);
    }

    @Test
    public void cross_site_POST_with_a_valid_session_cookie_is_rejected_before_authentication() {
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, sendRequest(Method.POST, HEADER_SEC_FETCH_SITE, "cross-site", HEADER_ORIGIN, "https://evil.example"));
        assertFalse("A rejected request should not reach the guard.", reachedTheGuard());
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, sendRequest(Method.POST, HEADER_ORIGIN, "https://evil.example"));
        assertFalse("A rejected request should not reach the guard.", reachedTheGuard());
        assertEquals(0, probe.requests);

        // A same-origin request with a cookie made the same way is accepted, so the cookies were valid and the rejections came from the filter.
        assertEquals(Status.SUCCESS_OK, sendRequest(Method.POST, HEADER_SEC_FETCH_SITE, "same-origin", HEADER_ORIGIN, HOST));
        assertTrue(reachedTheGuard());
        assertEquals(1, probe.requests);
    }

    @Test
    public void cross_site_GET_reaches_the_guarded_resource() {
        assertEquals(Status.SUCCESS_OK, sendRequest(Method.GET, HEADER_SEC_FETCH_SITE, "cross-site", HEADER_ORIGIN, "https://evil.example"));
        assertTrue(reachedTheGuard());
        assertEquals(1, probe.requests);
    }

    @Override
    public boolean saveDataPopulationScriptToFile() {
        return false;
    }

    @Override
    public boolean useSavedDataPopulationScript() {
        return false;
    }

}
