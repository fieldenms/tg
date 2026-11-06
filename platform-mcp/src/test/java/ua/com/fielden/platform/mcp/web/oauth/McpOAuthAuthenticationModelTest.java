package ua.com.fielden.platform.mcp.web.oauth;

import org.junit.Before;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.Method;
import org.restlet.engine.header.HeaderConstants;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.mcp.McpAccessToken;
import ua.com.fielden.platform.mcp.McpAccessTokenCo;
import ua.com.fielden.platform.mcp.McpAuthorisationCode;
import ua.com.fielden.platform.mcp.test_config.AbstractTgMcpOAuthTestCase;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;
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
    private final IMcpOAuth mcpOAuth = getInstance(IMcpOAuth.class);
    private final String mcpResource = mcpOAuth.protectedResourceMetadata().resource();

    @Before
    public void setNow() {
        constants.setNow(dateTime(NOW));
    }

    @Test
    public void access_token_issued_for_this_mcp_server_authenticates_its_user() {
        final var accessToken = saveAccessToken("token_for_this_server", mcpResource);
        assertThat(accessToken.isActive()).isTrue();
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

        assertRejected(result, response, "The access token was issued for another resource.");
    }

    @Test
    public void expired_access_token_is_rejected() {
        saveAccessToken("expired_token", mcpResource);
        constants.setNow(dateTime("2026-10-25 10:00:00"));
        final var accessToken = findAccessToken("expired_token");
        assertThat(isExpired(accessToken)).isTrue();

        final var request = request("expired_token");
        final var response = new Response(request);
        final var result = authModel.authenticate(request, response);

        assertRejected(result, response, "The access token expired.");
        assertUnchanged(accessToken);
    }

    @Test
    public void revoked_access_token_is_rejected() {
        coAccessToken().revoke(saveAccessToken("revoked_token", mcpResource));
        final var accessToken = findAccessToken("revoked_token");
        assertThat(accessToken.isActive()).isFalse();
        assertThat(isExpired(accessToken)).isFalse();

        final var request = request("revoked_token");
        final var response = new Response(request);
        final var result = authModel.authenticate(request, response);

        assertRejected(result, response, "The access token was revoked.");
    }

    @Test
    public void access_token_issued_to_unregistered_client_is_rejected() {
        final var accessToken = saveAccessToken("token_of_unregistered_client", getUser(), "unregistered_client", mcpResource);
        assertThat(mcpOAuth.validateClient(accessToken.getAuthorisationCode().getClientId()).isSuccessful()).isFalse();
        assertThat(accessToken.isActive()).isTrue();
        assertThat(isExpired(accessToken)).isFalse();

        final var request = request("token_of_unregistered_client");
        final var response = new Response(request);
        final var result = authModel.authenticate(request, response);

        assertRejected(result, response, "The client of the access token is not registered.");
        assertUnchanged(accessToken);
    }

    @Test
    public void access_token_of_inactive_user_is_rejected() {
        // Deactivating a user deactivates their access tokens, so an access token of an inactive user is active only if it was issued concurrently with the deactivation.
        // This is modelled by issuing the token in exchange for a code retrieved before the deactivation.
        final User user = save(new_(User.class, "MCP_USER").setBase(true).setEmail("mcp_user@unit-test.software").setActive(true));
        final var redeemedCode = saveRedeemedCode("code_of_inactive_user", user, TEST_CLIENT_ID, mcpResource);
        final IUser coUser = co$(User.class);
        save(coUser.findUser("MCP_USER").setActive(false));
        final var accessToken = saveAccessToken("token_of_inactive_user", redeemedCode);
        assertThat(accessToken.isActive()).isTrue();
        assertThat(accessToken.getAuthorisationCode().getUser().isActive()).isFalse();
        assertThat(isExpired(accessToken)).isFalse();

        final var request = request("token_of_inactive_user");
        final var response = new Response(request);
        final var result = authModel.authenticate(request, response);

        assertRejected(result, response, "The user of the access token is inactive.");
        assertUnchanged(accessToken);
    }

    private void assertRejected(final Result result, final Response response, final String expectedMessage) {
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getMessage()).isEqualTo(expectedMessage);
        assertThat(response.getStatus()).isEqualTo(CLIENT_ERROR_UNAUTHORIZED);
        assertThat(response.getChallengeRequests())
                .singleElement()
                .extracting(ChallengeRequest::getRawValue)
                .asString()
                .contains("error=\"invalid_token\"");
    }

    /// Asserts that `accessToken` has not been modified since it was retrieved.
    ///
    private void assertUnchanged(final McpAccessToken accessToken) {
        assertThat(coAccessToken().findById(accessToken.getId()).getVersion()).isEqualTo(accessToken.getVersion());
    }

    /// Saves an access token with value `tokenValue`, issued for `resource` in exchange for a code granted by the current user to the test client.
    ///
    private McpAccessToken saveAccessToken(final String tokenValue, final String resource) {
        return saveAccessToken(tokenValue, getUser(), TEST_CLIENT_ID, resource);
    }

    /// Saves an access token with value `tokenValue`, issued for `resource` in exchange for a code granted by `user` to `clientId`.
    ///
    private McpAccessToken saveAccessToken(final String tokenValue, final User user, final String clientId, final String resource) {
        return saveAccessToken(tokenValue, saveRedeemedCode("code_for_" + tokenValue, user, clientId, resource));
    }

    /// Saves an access token with value `tokenValue`, issued for the resource of `redeemedCode` in exchange for it.
    ///
    private McpAccessToken saveAccessToken(final String tokenValue, final McpAuthorisationCode redeemedCode) {
        final McpAccessTokenCo co = co$(McpAccessToken.class);
        return save(co.new_()
                      .setTokenHash(co.tokenHash(tokenValue))
                      .setExpirationDate(date("2026-10-24 10:00:00"))
                      .setAuthorisationCode(redeemedCode)
                      .setResource(redeemedCode.getResource()));
    }

    /// Saves code `code` for `resource`, granted by `user` to `clientId`, and redeems it.
    ///
    private McpAuthorisationCode saveRedeemedCode(final String code, final User user, final String clientId, final String resource) {
        final var savedCode = save(new_composite(McpAuthorisationCode.class, code)
                .setExpirationDate(date("2026-10-21 10:10:00"))
                .setUser(user)
                .setClientId(clientId)
                .setCodeChallenge("A".repeat(43))
                .setRedirectUri(TEST_CLIENT_REDIRECT_URI)
                .setResource(resource));
        return save(savedCode.setActive(false));
    }

    private McpAccessToken findAccessToken(final String tokenValue) {
        return coAccessToken().findByTokenValue(tokenValue).orElseThrow();
    }

    private boolean isExpired(final McpAccessToken accessToken) {
        return coAccessToken().isExpired(accessToken);
    }

    private McpAccessTokenCo coAccessToken() {
        return co(McpAccessToken.class);
    }

    private static Request request(final String tokenValue) {
        final var request = new Request(Method.POST, "http://localhost/mcp");
        request.getHeaders().add(HeaderConstants.HEADER_AUTHORIZATION, "Bearer " + tokenValue);
        return request;
    }

}
