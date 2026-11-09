package ua.com.fielden.platform.mcp.web.oauth;

import com.nimbusds.oauth2.sdk.pkce.CodeChallenge;
import com.nimbusds.oauth2.sdk.pkce.CodeChallengeMethod;
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;
import jakarta.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import ua.com.fielden.platform.mcp.McpAccessToken;
import ua.com.fielden.platform.mcp.McpAccessTokenCo;
import ua.com.fielden.platform.mcp.McpAuthorisationCode;
import ua.com.fielden.platform.mcp.test_config.AbstractTgMcpOAuthTestCase;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.IUniversalConstants;

import static org.assertj.core.api.Assertions.assertThat;
import static ua.com.fielden.platform.mcp.test_config.TgMcpOAuthTestCaseConfiguration.TEST_CLIENT_ID;
import static ua.com.fielden.platform.mcp.test_config.TgMcpOAuthTestCaseConfiguration.TEST_CLIENT_REDIRECT_URI;

public class McpTokenEndpointResourceTest extends AbstractTgMcpOAuthTestCase {

    private static final String NOW = "2026-10-21 10:00:00";
    private static final String ENDPOINT_URI = "http://localhost/oauth/token";
    private static final String CODE_VERIFIER = "code-verifier-0123456789-0123456789-0123456789";
    private static final String OTHER_CODE_VERIFIER = "other-code-verifier-0123456789-0123456789-0123456789";
    private static final String OTHER_RESOURCE = "https://other.com/mcp";

    private final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
    private final IUserProvider userProvider = getInstance(IUserProvider.class);
    private final String mcpResource = getInstance(IMcpOAuth.class).protectedResourceMetadata().resource();
    private final IDates dates = getInstance(IDates.class);

    @Before
    public void setNow() {
        constants.setNow(dateTime(NOW));
    }

    @Test
    public void token_request_without_resource_issues_access_token_for_mcp_server() {
        final var code = saveCode("code_for_mcp_server", TEST_CLIENT_ID, mcpResource);
        assertThat(code.isActive()).isTrue();
        assertNotExpired(code);

        final var response = requestToken("code_for_mcp_server", CODE_VERIFIER, null);

        assertThat(response.getStatus()).isEqualTo(Status.SUCCESS_OK);
        final var accessToken = findAccessToken(new JsonRepresentation(response.getEntity()).getJsonObject().getString("access_token"));
        assertThat(accessToken.getResource()).isEqualTo(mcpResource);
    }

    /// A request without `resource` targets the MCP server, rather than whatever resource the code was issued for.
    ///
    @Test
    public void token_request_without_resource_for_code_issued_for_another_resource_is_rejected() {
        final var code = saveCode("code_for_other_resource", TEST_CLIENT_ID, OTHER_RESOURCE);
        assertThat(code.isActive()).isTrue();
        assertNotExpired(code);
        assertThat(code.getResource()).isNotEqualTo(mcpResource);

        final var response = requestToken("code_for_other_resource", CODE_VERIFIER, null);

        assertThat(response.getStatus()).isEqualTo(Status.CLIENT_ERROR_BAD_REQUEST);
        assertThat(new JsonRepresentation(response.getEntity()).getJsonObject().getString("error")).isEqualTo("invalid_target");
        assertThat(coAccessToken().findByAuthorisationCode(code)).isEmpty();
    }

    @Test
    public void replay_of_authorisation_code_by_its_client_with_its_code_verifier_revokes_access_token_issued_from_it() {
        final var redeemedCode = saveRedeemedCode("replayed_code", TEST_CLIENT_ID);
        saveAccessToken("issued_token", redeemedCode);
        saveAccessToken("other_token", saveRedeemedCode("other_code", TEST_CLIENT_ID));
        assertRedeemed(redeemedCode);
        assertNotExpired(redeemedCode);
        assertThat(redeemedCode.getClientId()).isEqualTo(TEST_CLIENT_ID);
        assertThat(redeemedCode.getCodeChallenge()).isEqualTo(codeChallenge(CODE_VERIFIER));
        assertThat(findAccessToken("issued_token").isActive()).isTrue();
        assertThat(findAccessToken("other_token").isActive()).isTrue();

        final var response = requestToken("replayed_code", CODE_VERIFIER, mcpResource);

        assertReplayRejected(response);
        assertThat(findAccessToken("issued_token").isActive()).isFalse();
        assertThat(findAccessToken("other_token").isActive()).isTrue();
    }

    @Test
    public void replay_of_authorisation_code_with_another_code_verifier_does_not_revoke_access_token_issued_from_it() {
        final var redeemedCode = saveRedeemedCode("replayed_code", TEST_CLIENT_ID);
        saveAccessToken("issued_token", redeemedCode);
        assertRedeemed(redeemedCode);
        assertNotExpired(redeemedCode);
        assertThat(redeemedCode.getClientId()).isEqualTo(TEST_CLIENT_ID);
        assertThat(redeemedCode.getCodeChallenge()).isNotEqualTo(codeChallenge(OTHER_CODE_VERIFIER));

        final var response = requestToken("replayed_code", OTHER_CODE_VERIFIER, mcpResource);

        assertReplayRejected(response);
        assertThat(findAccessToken("issued_token").isActive()).isTrue();
    }

    @Test
    public void replay_of_authorisation_code_without_code_verifier_does_not_revoke_access_token_issued_from_it() {
        final var redeemedCode = saveRedeemedCode("replayed_code", TEST_CLIENT_ID);
        saveAccessToken("issued_token", redeemedCode);
        assertRedeemed(redeemedCode);
        assertNotExpired(redeemedCode);
        assertThat(redeemedCode.getClientId()).isEqualTo(TEST_CLIENT_ID);

        final var response = requestToken("replayed_code", null, mcpResource);

        assertReplayRejected(response);
        assertThat(findAccessToken("issued_token").isActive()).isTrue();
    }

    @Test
    public void replay_of_authorisation_code_by_another_client_does_not_revoke_access_token_issued_from_it() {
        final var redeemedCode = saveRedeemedCode("replayed_code", "other_client");
        saveAccessToken("issued_token", redeemedCode);
        assertRedeemed(redeemedCode);
        assertNotExpired(redeemedCode);
        assertThat(redeemedCode.getClientId()).isNotEqualTo(TEST_CLIENT_ID);
        assertThat(redeemedCode.getCodeChallenge()).isEqualTo(codeChallenge(CODE_VERIFIER));

        final var response = requestToken("replayed_code", CODE_VERIFIER, mcpResource);

        assertReplayRejected(response);
        assertThat(findAccessToken("issued_token").isActive()).isTrue();
    }

    @Test
    public void replay_of_authorisation_code_from_which_no_access_token_was_issued_is_rejected() {
        final var redeemedCode = saveRedeemedCode("replayed_code", TEST_CLIENT_ID);
        assertRedeemed(redeemedCode);
        assertNotExpired(redeemedCode);
        assertThat(redeemedCode.getClientId()).isEqualTo(TEST_CLIENT_ID);
        assertThat(redeemedCode.getCodeChallenge()).isEqualTo(codeChallenge(CODE_VERIFIER));
        assertThat(coAccessToken().findByAuthorisationCode(redeemedCode)).isEmpty();

        final var response = requestToken("replayed_code", CODE_VERIFIER, mcpResource);

        assertReplayRejected(response);
    }

    /// Asserts that a request presenting `code` reaches the check for a replay.
    ///
    private void assertRedeemed(final McpAuthorisationCode code) {
        assertThat(code.isActive()).isFalse();
    }

    private void assertNotExpired(final McpAuthorisationCode code) {
        assertThat(code.getExpirationDate()).isAfter(dates.serverNow().toDate());
    }

    private static void assertReplayRejected(final Response response) {
        assertThat(response.getStatus()).isEqualTo(Status.CLIENT_ERROR_BAD_REQUEST);
        final var body = new JsonRepresentation(response.getEntity()).getJsonObject();
        assertThat(body.getString("error")).isEqualTo("invalid_grant");
        assertThat(body.getString("error_description")).isEqualTo("Authorisation code was already redeemed.");
    }

    /// Sends a token request from the test client, presenting `code`, and `codeVerifier` and `resource` if they are not `null`.
    /// The request is handled without a current user, as the token endpoint is not guarded.
    ///
    private Response requestToken(final String code, final @Nullable String codeVerifier, final @Nullable String resource) {
        final var form = new Form();
        form.add("grant_type", "authorization_code");
        form.add("client_id", TEST_CLIENT_ID);
        form.add("code", code);
        if (codeVerifier != null) {
            form.add("code_verifier", codeVerifier);
        }
        form.add("redirect_uri", TEST_CLIENT_REDIRECT_URI);
        if (resource != null) {
            form.add("resource", resource);
        }
        final var request = new Request(Method.POST, ENDPOINT_URI, form.getWebRepresentation());
        final var response = new Response(request);
        final var testUser = userProvider.getUser();
        userProvider.clearUser();
        try {
            getInstance(McpTokenEndpointResource.Factory.class).handle(request, response);
        } finally {
            userProvider.setUser(testUser);
        }
        return response;
    }

    /// Saves an authorisation code with value `code`, granted by the current user to `clientId` with the code challenge for [#CODE_VERIFIER], and redeems it.
    ///
    private McpAuthorisationCode saveRedeemedCode(final String code, final String clientId) {
        return save(saveCode(code, clientId, mcpResource).setActive(false));
    }

    /// Saves an authorisation code with value `code` for `resource`, granted by the current user to `clientId` with the code challenge for [#CODE_VERIFIER].
    ///
    private McpAuthorisationCode saveCode(final String code, final String clientId, final String resource) {
        return save(new_composite(McpAuthorisationCode.class, code)
                .setExpirationDate(date("2026-10-21 10:10:00"))
                .setUser(getUser())
                .setClientId(clientId)
                .setCodeChallenge(codeChallenge(CODE_VERIFIER))
                .setRedirectUri(TEST_CLIENT_REDIRECT_URI)
                .setResource(resource));
    }

    /// Saves an access token with value `tokenValue`, issued in exchange for `redeemedCode`.
    ///
    private McpAccessToken saveAccessToken(final String tokenValue, final McpAuthorisationCode redeemedCode) {
        final McpAccessTokenCo co = co$(McpAccessToken.class);
        return save(co.new_()
                      .setTokenHash(co.tokenHash(tokenValue))
                      .setExpirationDate(date("2026-10-24 10:00:00"))
                      .setAuthorisationCode(redeemedCode)
                      .setResource(redeemedCode.getResource()));
    }

    private McpAccessToken findAccessToken(final String tokenValue) {
        return coAccessToken().findByTokenValue(tokenValue).orElseThrow();
    }

    private McpAccessTokenCo coAccessToken() {
        return co(McpAccessToken.class);
    }

    private static String codeChallenge(final String codeVerifier) {
        return CodeChallenge.compute(CodeChallengeMethod.S256, new CodeVerifier(codeVerifier)).getValue();
    }

}
