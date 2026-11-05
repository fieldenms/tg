package ua.com.fielden.platform.mcp.validators;

import org.junit.Before;
import org.junit.Test;
import ua.com.fielden.platform.mcp.McpAccessToken;
import ua.com.fielden.platform.mcp.McpAuthorisationCode;
import ua.com.fielden.platform.mcp.McpAuthorisationCodeCo;
import ua.com.fielden.platform.mcp.test_config.AbstractTgMcpServerTestCase;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.IUniversalConstants;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static ua.com.fielden.platform.mcp.McpAccessToken.AUTHORISATION_CODE;
import static ua.com.fielden.platform.mcp.validators.McpAccessTokenActiveUserValidator.ERR_USER_INACTIVE;
import static ua.com.fielden.platform.mcp.validators.McpAccessTokenAuthorisationCodeValidator.ERR_CODE_EXPIRED;
import static ua.com.fielden.platform.mcp.validators.McpAccessTokenAuthorisationCodeValidator.ERR_CODE_NOT_REDEEMED;

public class McpAccessTokenAuthorisationCodeValidationTest extends AbstractTgMcpServerTestCase {

    private static final String NOW = "2026-10-21 10:00:00";

    private final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
    private final IDates dates = getInstance(IDates.class);

    @Before
    public void setNow() {
        constants.setNow(dateTime(NOW));
    }

    @Test
    public void active_authorisation_code_is_rejected() {
        final var code = saveCode("ACTIVE_CODE", getUser(), "2026-10-21 10:10:00");
        assertThat(code.isActive()).isTrue();
        assertThat(code.getExpirationDate()).isAfter(serverNow());

        final McpAccessToken accessToken = new_(McpAccessToken.class);
        accessToken.setAuthorisationCode(code);

        assertCodeRejected(accessToken, ERR_CODE_NOT_REDEEMED);
    }

    @Test
    public void redeemed_unexpired_authorisation_code_of_active_user_is_accepted() {
        final var code = save(saveCode("REDEEMED_CODE", getUser(), "2026-10-21 10:10:00").setActive(false));
        assertThat(code.isActive()).isFalse();
        assertThat(code.getExpirationDate()).isAfter(serverNow());
        assertThat(code.getUser().isActive()).isTrue();

        final McpAccessToken accessToken = new_(McpAccessToken.class);
        accessToken.setAuthorisationCode(code);

        assertThat(accessToken.getProperty(AUTHORISATION_CODE).isValid()).isTrue();
        assertThat(accessToken.getAuthorisationCode()).isEqualTo(code);
    }

    @Test
    public void expired_authorisation_code_is_rejected() {
        final var code = save(saveCode("EXPIRED_CODE", getUser(), "2026-10-21 09:59:00").setActive(false));
        assertThat(code.isActive()).isFalse();
        assertThat(code.getExpirationDate()).isBeforeOrEqualTo(serverNow());

        final McpAccessToken accessToken = new_(McpAccessToken.class);
        accessToken.setAuthorisationCode(code);

        assertCodeRejected(accessToken, ERR_CODE_EXPIRED);
    }

    @Test
    public void authorisation_code_is_expired_at_its_expiration_date() {
        final var code = save(saveCode("CODE_EXPIRING_NOW", getUser(), NOW).setActive(false));
        assertThat(code.isActive()).isFalse();
        assertThat(code.getExpirationDate()).hasSameTimeAs(serverNow());

        final McpAccessToken accessToken = new_(McpAccessToken.class);
        accessToken.setAuthorisationCode(code);

        assertCodeRejected(accessToken, ERR_CODE_EXPIRED);
    }

    @Test
    public void redeemed_authorisation_code_of_inactive_user_is_rejected() {
        final User user = save(new_(User.class, "MCP_USER").setBase(true).setEmail("mcp_user@unit-test.software").setActive(true));
        save(saveCode("CODE_OF_INACTIVE_USER", user, "2026-10-21 10:10:00").setActive(false));
        final IUser coUser = co$(User.class);
        save(coUser.findUser("MCP_USER").setActive(false));
        final McpAuthorisationCodeCo coCode = co(McpAuthorisationCode.class);
        final var code = coCode.findByKeyAndFetch(McpAuthorisationCodeCo.FETCH_PROVIDER.fetchModel(), "CODE_OF_INACTIVE_USER");
        assertThat(code.isActive()).isFalse();
        assertThat(code.getExpirationDate()).isAfter(serverNow());
        assertThat(code.getUser().isActive()).isFalse();

        final McpAccessToken accessToken = new_(McpAccessToken.class);
        accessToken.setAuthorisationCode(code);

        assertCodeRejected(accessToken, ERR_USER_INACTIVE.formatted("MCP_USER"));
    }

    private McpAuthorisationCode saveCode(final String code, final User user, final String expirationDate) {
        return save(new_composite(McpAuthorisationCode.class, code)
                .setExpirationDate(date(expirationDate))
                .setUser(user)
                .setClientId("test_client")
                .setCodeChallenge("A".repeat(43))
                .setRedirectUri("http://localhost/callback")
                .setResource("https://tgdev.com/mcp"));
    }

    /// The current time, as the validators see it.
    ///
    private Date serverNow() {
        return dates.serverNow().toDate();
    }

    private static void assertCodeRejected(final McpAccessToken accessToken, final String expectedError) {
        final var authorisationCodeProp = accessToken.getProperty(AUTHORISATION_CODE);
        assertThat(authorisationCodeProp.isValid()).isFalse();
        assertThat(authorisationCodeProp.getFirstFailure().getMessage()).isEqualTo(expectedError);
        assertThat(accessToken.getAuthorisationCode()).isNull();
    }

}
