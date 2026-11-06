package ua.com.fielden.platform.mcp;

import org.junit.Before;
import org.junit.Test;
import ua.com.fielden.platform.mcp.test_config.AbstractTgMcpServerTestCase;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.utils.IUniversalConstants;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/// Tests the effect of deactivating and reactivating a user on the MCP authorisation codes and access tokens of the authorisations granted by that user.
///
public class McpUserDeactivationTest extends AbstractTgMcpServerTestCase {

    private static final String NOW = "2026-10-21 10:00:00";

    private final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);

    @Before
    public void setNow() {
        constants.setNow(dateTime(NOW));
    }

    @Test
    public void deactivating_a_user_deactivates_access_tokens_issued_from_authorisations_granted_by_that_user() {
        final User user = saveUser("MCP_USER");
        saveAccessToken("token_1_of_mcp_user", user);
        saveAccessToken("token_2_of_mcp_user", user);
        saveAccessToken("token_of_other_user", getUser());
        final var tokenValues = List.of("token_1_of_mcp_user", "token_2_of_mcp_user", "token_of_other_user");
        assertThat(tokenValues).map(this::isAccessTokenActive).containsExactly(true, true, true);

        deactivateUser("MCP_USER");

        assertThat(tokenValues).map(this::isAccessTokenActive).containsExactly(false, false, true);
    }

    @Test
    public void deactivating_a_user_deactivates_authorisation_codes_granted_by_that_user() {
        final User user = saveUser("MCP_USER");
        saveCode("code_of_mcp_user", user);
        saveCode("code_of_other_user", getUser());
        final var codes = List.of("code_of_mcp_user", "code_of_other_user");
        assertThat(codes).map(this::isCodeActive).containsExactly(true, true);

        deactivateUser("MCP_USER");

        assertThat(codes).map(this::isCodeActive).containsExactly(false, true);
    }

    @Test
    public void reactivating_a_user_leaves_access_tokens_issued_from_authorisations_granted_by_that_user_inactive() {
        final User user = saveUser("MCP_USER");
        saveAccessToken("token_of_mcp_user", user);
        deactivateUser("MCP_USER");
        assertThat(isAccessTokenActive("token_of_mcp_user")).isFalse();

        final var reactivatedUser = activateUser("MCP_USER");

        assertThat(reactivatedUser.isActive()).isTrue();
        assertThat(isAccessTokenActive("token_of_mcp_user")).isFalse();
    }

    @Test
    public void reactivating_a_user_leaves_authorisation_codes_granted_by_that_user_inactive() {
        final User user = saveUser("MCP_USER");
        saveCode("code_of_mcp_user", user);
        deactivateUser("MCP_USER");
        assertThat(isCodeActive("code_of_mcp_user")).isFalse();

        final var reactivatedUser = activateUser("MCP_USER");

        assertThat(reactivatedUser.isActive()).isTrue();
        assertThat(isCodeActive("code_of_mcp_user")).isFalse();
    }

    private User saveUser(final String username) {
        return save(new_(User.class, username).setBase(true).setEmail("%s@unit-test.software".formatted(username.toLowerCase())).setActive(true));
    }

    private void deactivateUser(final String username) {
        final IUser coUser = co$(User.class);
        save(coUser.findUser(username).setActive(false));
    }

    private User activateUser(final String username) {
        final IUser coUser = co$(User.class);
        return save(coUser.findUser(username).setActive(true));
    }

    /// Saves an authorisation code granted by `user`, which is yet to be redeemed.
    ///
    private McpAuthorisationCode saveCode(final String code, final User user) {
        return save(new_composite(McpAuthorisationCode.class, code)
                .setExpirationDate(date("2026-10-21 10:10:00"))
                .setUser(user)
                .setClientId("test_client")
                .setCodeChallenge("A".repeat(43))
                .setRedirectUri("http://localhost/callback")
                .setResource("https://tgdev.com/mcp"));
    }

    /// Saves an access token with value `tokenValue`, issued in exchange for a code granted by `user`.
    ///
    private McpAccessToken saveAccessToken(final String tokenValue, final User user) {
        final var redeemedCode = save(saveCode("code_for_" + tokenValue, user).setActive(false));
        final McpAccessTokenCo co = co$(McpAccessToken.class);
        return save(co.new_()
                      .setTokenHash(co.tokenHash(tokenValue))
                      .setExpirationDate(date("2026-10-24 10:00:00"))
                      .setAuthorisationCode(redeemedCode)
                      .setResource(redeemedCode.getResource()));
    }

    private boolean isAccessTokenActive(final String tokenValue) {
        final McpAccessTokenCo co = co(McpAccessToken.class);
        return co.findByTokenValue(tokenValue).orElseThrow().isActive();
    }

    private boolean isCodeActive(final String code) {
        final McpAuthorisationCodeCo co = co(McpAuthorisationCode.class);
        return co.findByKey(code).isActive();
    }

}
