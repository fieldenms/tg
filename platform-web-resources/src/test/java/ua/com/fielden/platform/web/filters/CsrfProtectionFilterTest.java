package ua.com.fielden.platform.web.filters;

import org.junit.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Status;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.web.filters.CsrfProtectionFilter.*;

/// Tests for [CsrfProtectionFilter].
///
public class CsrfProtectionFilterTest {

    private static final String HOST = "https://tgdev.com";

    /// The next restlet in the chain, which marks a request as handled.
    ///
    private static final Restlet NEXT = new Restlet() {
        @Override
        public void handle(final Request request, final Response response) {
            response.setStatus(Status.SUCCESS_OK);
        }
    };

    private static Status handle(final Method method, final String hostRef, final String... headers) {
        final var request = new Request(method, hostRef + "/entity/ua.com.fielden.platform.security.user.UserRole/new");
        request.setHostRef(hostRef);
        for (int index = 0; index < headers.length; index += 2) {
            request.getHeaders().add(headers[index], headers[index + 1]);
        }
        final var response = new Response(request);
        new CsrfProtectionFilter(null, NEXT).handle(request, response);
        return response.getStatus();
    }

    private static Status post(final String... headers) {
        return handle(Method.POST, HOST, headers);
    }

    @Test
    public void safe_methods_pass_even_if_cross_site() {
        for (final Method method : new Method[] {Method.GET, Method.HEAD, Method.OPTIONS}) {
            assertEquals(method.getName(), Status.SUCCESS_OK, handle(method, HOST, HEADER_SEC_FETCH_SITE, "cross-site", HEADER_ORIGIN, "https://evil.example"));
        }
    }

    @Test
    public void unsafe_methods_are_rejected_if_cross_site() {
        for (final Method method : new Method[] {Method.POST, Method.PUT, Method.DELETE, Method.PATCH}) {
            assertEquals(method.getName(), Status.CLIENT_ERROR_FORBIDDEN, handle(method, HOST, HEADER_SEC_FETCH_SITE, "cross-site"));
        }
    }

    @Test
    public void Sec_Fetch_Site_same_origin_or_none_passes() {
        assertEquals(Status.SUCCESS_OK, post(HEADER_SEC_FETCH_SITE, "same-origin", HEADER_ORIGIN, HOST));
        assertEquals(Status.SUCCESS_OK, post(HEADER_SEC_FETCH_SITE, "none"));
        assertEquals(Status.SUCCESS_OK, post(HEADER_SEC_FETCH_SITE, "Same-Origin"));
    }

    @Test
    public void Sec_Fetch_Site_same_site_or_cross_site_is_rejected() {
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, post(HEADER_SEC_FETCH_SITE, "same-site", HEADER_ORIGIN, "https://other.tgdev.com"));
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, post(HEADER_SEC_FETCH_SITE, "cross-site", HEADER_ORIGIN, "https://evil.example"));
    }

    @Test
    public void Sec_Fetch_Site_takes_precedence_over_Origin() {
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, post(HEADER_SEC_FETCH_SITE, "cross-site", HEADER_ORIGIN, HOST));
    }

    @Test
    public void without_Sec_Fetch_Site_Origin_matching_the_host_passes() {
        assertEquals(Status.SUCCESS_OK, post(HEADER_ORIGIN, HOST));
        assertEquals(Status.SUCCESS_OK, post(HEADER_ORIGIN, "https://TGDEV.com"));
    }

    @Test
    public void without_Sec_Fetch_Site_Origin_not_matching_the_host_is_rejected() {
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, post(HEADER_ORIGIN, "https://evil.example"));
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, post(HEADER_ORIGIN, "https://tgdev.com.evil.example"));
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, post(HEADER_ORIGIN, "null"));
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, post(HEADER_ORIGIN, "not a URI"));
    }

    @Test
    public void ports_are_compared_if_the_request_host_specifies_one() {
        final String hostWithPort = "https://localhost:8091";
        assertEquals(Status.SUCCESS_OK, handle(Method.POST, hostWithPort, HEADER_ORIGIN, "https://localhost:8091"));
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, handle(Method.POST, hostWithPort, HEADER_ORIGIN, "https://localhost:9000"));
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, handle(Method.POST, hostWithPort, HEADER_ORIGIN, "https://localhost"));
        assertEquals(Status.SUCCESS_OK, handle(Method.POST, "http://localhost:443", HEADER_ORIGIN, "https://localhost"));
    }

    @Test
    public void X_Forwarded_Host_takes_precedence_over_the_request_host() {
        final String backend = "http://backend:8091";
        assertEquals(Status.SUCCESS_OK, handle(Method.POST, backend, HEADER_ORIGIN, HOST, HEADER_X_FORWARDED_HOST, "tgdev.com"));
        assertEquals(Status.SUCCESS_OK, handle(Method.POST, backend, HEADER_ORIGIN, HOST, HEADER_X_FORWARDED_HOST, "tgdev.com, proxy.internal"));
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, handle(Method.POST, backend, HEADER_ORIGIN, "https://evil.example", HEADER_X_FORWARDED_HOST, "tgdev.com"));
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, handle(Method.POST, backend, HEADER_ORIGIN, HOST));
    }

    @Test
    public void unsafe_requests_without_Sec_Fetch_Site_and_Origin_are_rejected() {
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, post());
    }

    @Test
    public void safe_requests_without_Sec_Fetch_Site_and_Origin_pass() {
        assertEquals(Status.SUCCESS_OK, handle(Method.GET, HOST));
    }

}
