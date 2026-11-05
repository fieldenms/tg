package ua.com.fielden.platform.mcp.web.oauth;

import org.junit.Before;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.Method;
import org.restlet.engine.header.HeaderConstants;
import ua.com.fielden.platform.mcp.McpAccessToken;
import ua.com.fielden.platform.mcp.McpAccessTokenCo;
import ua.com.fielden.platform.mcp.McpAuthorisationCode;
import ua.com.fielden.platform.mcp.test_config.AbstractTgMcpOAuthTestCase;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.utils.IUniversalConstants;

import static org.assertj.core.api.Assertions.assertThat;
import static org.restlet.data.Status.CLIENT_ERROR_UNAUTHORIZED;
import static ua.com.fielden.platform.mcp.test_config.TgMcpOAuthTestCaseConfiguration.TEST_CLIENT_ID;
import static ua.com.fielden.platform.mcp.test_config.TgMcpOAuthTestCaseConfiguration.TEST_CLIENT_REDIRECT_URI;

public class McpOAuthAuthenticationModelTest extends AbstractTgMcpOAuthTestCase {

    private static final String NOW = "2026-10-21 10:00:00";
    private static final String OTHER_RESOURCE = "https://other.com/mcp";

    private final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
    private final McpOAuthAuthenticationModel authModel = getInstance(McpOAuthAuthenticationModel.class);
    private final String mcpResource = getInstance(IMcpOAuth.class).protectedResourceMetadata().resource();

    @Before
    public void setNow() {
        constants.setNow(dateTime(NOW));
    }

    @Test
    public void access_token_issued_for_this_mcp_server_authenticates_its_user() {
        final var accessToken = saveAccessToken("token_for_this_server", mcpResource);
        assertThat(accessToken.getResource()).isEqualTo(mcpResource);
        assertThat(isExpired(accessToken)).isFalse();
        assertThat(accessToken.getAuthorisationCode().getUser().isActive()).isTrue();

        final var request = request("token_for_this_server");
        final var response = new Response(request);
        final var result = authModel.authenticate(request, response);

        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getInstance()).isEqualTo(getUser());
        assertThat(response.getChallengeRequests()).isEmpty();
    }

    @Test
    public void access_token_issued_for_another_resource_is_rejected() {
        final var accessToken = saveAccessToken("token_for_another_resource", OTHER_RESOURCE);
        assertThat(accessToken.getResource()).isNotEqualTo(mcpResource);
        assertThat(isExpired(accessToken)).isFalse();

        final var request = request("token_for_another_resource");
        final var response = new Response(request);
        final var result = authModel.authenticate(request, response);

        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getMessage()).isEqualTo("The access token was issued for another resource.");
        assertThat(response.getStatus()).isEqualTo(CLIENT_ERROR_UNAUTHORIZED);
        assertThat(response.getChallengeRequests())
                .singleElement()
                .extracting(ChallengeRequest::getRawValue)
                .asString()
                .contains("error=\"invalid_token\"");
    }

    /// Saves an access token with value `tokenValue`, issued for `resource` in exchange for a code granted by the current user.
    ///
    private McpAccessToken saveAccessToken(final String tokenValue, final String resource) {
        final var code = save(new_composite(McpAuthorisationCode.class, "code_for_" + tokenValue)
                .setExpirationDate(date("2026-10-21 10:10:00"))
                .setUser(getUser())
                .setClientId(TEST_CLIENT_ID)
                .setCodeChallenge("A".repeat(43))
                .setRedirectUri(TEST_CLIENT_REDIRECT_URI)
                .setResource(resource));
        final var redeemedCode = save(code.setActive(false));

        final McpAccessTokenCo co = co$(McpAccessToken.class);
        return save(co.new_()
                      .setTokenHash(co.tokenHash(tokenValue))
                      .setExpirationDate(date("2026-10-24 10:00:00"))
                      .setAuthorisationCode(redeemedCode)
                      .setResource(resource));
    }

    private boolean isExpired(final McpAccessToken accessToken) {
        final McpAccessTokenCo co = co(McpAccessToken.class);
        return co.isExpired(accessToken);
    }

    private static Request request(final String tokenValue) {
        final var request = new Request(Method.POST, "http://localhost/mcp");
        request.getHeaders().add(HeaderConstants.HEADER_AUTHORIZATION, "Bearer " + tokenValue);
        return request;
    }

}
