package ua.com.fielden.platform.mcp;

import org.junit.Before;
import org.junit.Test;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.mcp.test_config.AbstractTgMcpServerTestCase;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.utils.IUniversalConstants;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ua.com.fielden.platform.companion.PersistentEntitySaver.ERR_COULD_NOT_RESOLVE_CONFLICTING_CHANGES;

public class McpAccessTokenDaoTest extends AbstractTgMcpServerTestCase {

    private static final String NOW = "2026-10-21 10:00:00";

    private final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);

    @Before
    public void setNow() {
        constants.setNow(dateTime(NOW));
    }

    @Test
    public void new_access_token_is_active() {
        final McpAccessTokenCo co = co$(McpAccessToken.class);
        assertThat(co.new_().isActive()).isTrue();
    }

    @Test
    public void of_concurrent_changes_to_the_active_status_of_an_access_token_only_the_first_is_saved() {
        saveAccessToken("TOKEN");
        final McpAccessTokenCo co = co$(McpAccessToken.class);
        final var tokenKey = co.tokenHash("TOKEN");
        final var accessToken = co.findByKeyAndFetch(McpAccessTokenCo.FETCH_PROVIDER.fetchModel(), tokenKey);
        final var concurrentAccessToken = co.findByKeyAndFetch(McpAccessTokenCo.FETCH_PROVIDER.fetchModel(), tokenKey);
        assertThat(accessToken.isActive()).isTrue();
        assertThat(concurrentAccessToken.getVersion()).isEqualTo(accessToken.getVersion());

        save(accessToken.setActive(false));

        assertThatThrownBy(() -> save(concurrentAccessToken.setActive(false)))
                .isInstanceOf(EntityCompanionException.class)
                .hasMessageStartingWith(ERR_COULD_NOT_RESOLVE_CONFLICTING_CHANGES);

        assertThat(co.findByKey(tokenKey).isActive()).isFalse();
    }

    /// Saves an access token with value `tokenValue`, issued in exchange for a code granted by the current user.
    ///
    private McpAccessToken saveAccessToken(final String tokenValue) {
        final var code = save(new_composite(McpAuthorisationCode.class, "code_for_" + tokenValue)
                .setExpirationDate(date("2026-10-21 10:10:00"))
                .setUser(getUser())
                .setClientId("test_client")
                .setCodeChallenge("A".repeat(43))
                .setRedirectUri("http://localhost/callback")
                .setResource("https://tgdev.com/mcp"));
        final var redeemedCode = save(code.setActive(false));

        final McpAccessTokenCo co = co$(McpAccessToken.class);
        return save(co.new_()
                      .setTokenHash(co.tokenHash(tokenValue))
                      .setExpirationDate(date("2026-10-24 10:00:00"))
                      .setAuthorisationCode(redeemedCode)
                      .setResource("https://tgdev.com/mcp"));
    }

}
