package ua.com.fielden.platform.web.factories.webui;

import com.google.inject.Injector;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Status;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.test.AbstractWebResourceWithDaoTestCase;
import ua.com.fielden.platform.web.resources.webui.LoginInitiateResetResource;
import ua.com.fielden.platform.web.resources.webui.LoginResource;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.web.filters.CsrfProtectionFilter.HEADER_ORIGIN;
import static ua.com.fielden.platform.web.filters.CsrfProtectionFilter.HEADER_SEC_FETCH_SITE;

/// Tests that the factories of the login and password reset resources, which applications attach outside of
/// [ua.com.fielden.platform.web.filters.CsrfProtectionFilter], reject forged cross-site requests.
/// Which requests are forged is covered by [ua.com.fielden.platform.web.filters.CsrfProtectionFilterTest].
///
public class LoginResourceFactoriesCsrfTest extends AbstractWebResourceWithDaoTestCase {

    private static final String HOST = "https://tgdev.com";

    private static Response post(final Restlet factory, final String path, final String... headers) {
        final var form = new Form();
        form.add("username", "UNKNOWN_USER");
        form.add("passwd", "invalid password");
        final var request = new Request(Method.POST, HOST + path, form.getWebRepresentation());
        request.setHostRef(HOST);
        for (int index = 0; index < headers.length; index += 2) {
            request.getHeaders().add(headers[index], headers[index + 1]);
        }
        final var response = new Response(request);
        factory.handle(request, response);
        return response;
    }

    private static void assertRejected(final Response response) {
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
        assertNull("The resource should not be reached.", response.getEntity());
    }

    @Test
    public void cross_site_login_is_rejected() {
        final var factory = new LoginResourceFactory(getInstance(RestServerUtil.class), getInstance(Injector.class));
        assertRejected(post(factory, LoginResource.BINDING_PATH, HEADER_SEC_FETCH_SITE, "cross-site", HEADER_ORIGIN, "https://evil.example"));
        assertRejected(post(factory, LoginResource.BINDING_PATH, HEADER_ORIGIN, "https://evil.example"));
        assertRejected(post(factory, LoginResource.BINDING_PATH));
    }

    @Test
    public void cross_site_password_reset_initiation_is_rejected() {
        final var factory = new LoginInitiateResetResourceFactory(getInstance(Injector.class));
        assertRejected(post(factory, LoginInitiateResetResource.BINDING_PATH, HEADER_SEC_FETCH_SITE, "cross-site", HEADER_ORIGIN, "https://evil.example"));
    }

    @Test
    public void cross_site_password_reset_completion_is_rejected() {
        final var factory = new LoginCompleteResetResourceFactory(getInstance(Injector.class), "demo secret");
        assertRejected(post(factory, "/reset_password/some-uuid", HEADER_SEC_FETCH_SITE, "cross-site", HEADER_ORIGIN, "https://evil.example"));
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
