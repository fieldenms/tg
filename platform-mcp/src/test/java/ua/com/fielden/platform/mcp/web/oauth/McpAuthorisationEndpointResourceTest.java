package ua.com.fielden.platform.mcp.web.oauth;

import org.junit.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.*;
import ua.com.fielden.platform.mcp.test_config.AbstractTgMcpOAuthTestCase;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static ua.com.fielden.platform.mcp.test_config.TgMcpOAuthTestCaseConfiguration.TEST_CLIENT_ID;
import static ua.com.fielden.platform.mcp.test_config.TgMcpOAuthTestCaseConfiguration.TEST_CLIENT_REDIRECT_URI;
import static ua.com.fielden.platform.mcp.web.oauth.McpAuthorisationEndpointResource.ERR_INVALID_CLIENT;

public class McpAuthorisationEndpointResourceTest extends AbstractTgMcpOAuthTestCase {

    private static final String INJECTED_TEXT = "<script>alert(1)</script> Your session has expired.";
    private static final String ENDPOINT_URI = "http://localhost/oauth/authorize";

    @Test
    public void authorisation_request_from_unknown_client_is_rejected_without_reflecting_request_values() throws IOException {
        final var response = handle(new Request(Method.GET, ENDPOINT_URI + "?" + form(INJECTED_TEXT, TEST_CLIENT_REDIRECT_URI).getQueryString()));
        assertInvalidClientResponse(response);
    }

    @Test
    public void authorisation_request_with_unregistered_redirect_uri_is_rejected_without_reflecting_request_values() throws IOException {
        final var response = handle(new Request(Method.GET, ENDPOINT_URI + "?" + form(TEST_CLIENT_ID, "http://localhost/" + INJECTED_TEXT).getQueryString()));
        assertInvalidClientResponse(response);
    }

    @Test
    public void consent_submission_from_unknown_client_is_rejected_without_reflecting_request_values() throws IOException {
        final var form = form(INJECTED_TEXT, TEST_CLIENT_REDIRECT_URI);
        form.add("decision", "allow");
        final var response = handle(new Request(Method.POST, ENDPOINT_URI, form.getWebRepresentation()));
        assertInvalidClientResponse(response);
    }

    /// The client is valid, so the remaining validation reports the missing parameters by redirecting to the client.
    ///
    @Test
    public void authorisation_request_from_registered_client_with_registered_redirect_uri_passes_client_validation() {
        final var response = handle(new Request(Method.GET, ENDPOINT_URI + "?" + form(TEST_CLIENT_ID, TEST_CLIENT_REDIRECT_URI).getQueryString()));
        assertThat(response.getStatus()).isEqualTo(Status.REDIRECTION_FOUND);
        assertThat(response.getLocationRef().toString()).startsWith(TEST_CLIENT_REDIRECT_URI + "?");
    }

    private Response handle(final Request request) {
        final var response = new Response(request);
        getInstance(McpAuthorisationEndpointResource.Factory.class).handle(request, response);
        return response;
    }

    private static Form form(final String clientId, final String redirectUri) {
        final var form = new Form();
        form.add("client_id", clientId);
        form.add("redirect_uri", redirectUri);
        return form;
    }

    private static void assertInvalidClientResponse(final Response response) throws IOException {
        assertThat(response.getStatus()).isEqualTo(Status.CLIENT_ERROR_BAD_REQUEST);
        assertThat(response.getEntity().getMediaType()).isEqualTo(MediaType.TEXT_PLAIN);
        assertThat(response.getEntity().getText()).isEqualTo(ERR_INVALID_CLIENT);
        assertThat(response.getHeaders().getFirstValue("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(response.getCacheDirectives()).extracting(CacheDirective::getName).contains("no-store");
    }

}
