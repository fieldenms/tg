package ua.com.fielden.platform.mcp.validators;

import org.junit.Before;
import org.junit.Test;
import ua.com.fielden.platform.mcp.McpAccessToken;
import ua.com.fielden.platform.mcp.McpAccessTokenCo;
import ua.com.fielden.platform.mcp.McpAuthorisationCode;
import ua.com.fielden.platform.mcp.test_config.AbstractTgMcpServerTestCase;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.utils.IUniversalConstants;

import static org.assertj.core.api.Assertions.assertThat;
import static ua.com.fielden.platform.mcp.McpAccessToken.RESOURCE;
import static ua.com.fielden.platform.mcp.validators.McpAccessTokenResourceValidator.ERR_RESOURCE_MISMATCH;

public class McpAccessTokenResourceValidationTest extends AbstractTgMcpServerTestCase {

    private static final String NOW = "2026-10-21 10:00:00";
    private static final String
            CODE_RESOURCE = "https://tgdev.com/mcp",
            OTHER_RESOURCE = "https://other.com/mcp";

    private final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);

    @Before
    public void setNow() {
        constants.setNow(dateTime(NOW));
    }

    @Test
    public void resource_of_the_authorisation_code_is_accepted() {
        final var code = saveRedeemedCode("CODE_1", CODE_RESOURCE);
        assertThat(code.getResource()).isEqualTo(CODE_RESOURCE);
        final McpAccessToken accessToken = new_(McpAccessToken.class).setAuthorisationCode(code);
        assertThat(accessToken.getAuthorisationCode()).isEqualTo(code);

        accessToken.setResource(CODE_RESOURCE);

        assertThat(accessToken.getProperty(RESOURCE).isValid()).isTrue();
        assertThat(accessToken.getResource()).isEqualTo(CODE_RESOURCE);
    }

    @Test
    public void resource_other_than_that_of_the_authorisation_code_is_rejected() {
        final var code = saveRedeemedCode("CODE_2", CODE_RESOURCE);
        assertThat(code.getResource()).isNotEqualTo(OTHER_RESOURCE);
        final McpAccessToken accessToken = new_(McpAccessToken.class).setAuthorisationCode(code);
        assertThat(accessToken.getAuthorisationCode()).isEqualTo(code);

        accessToken.setResource(OTHER_RESOURCE);

        final var resourceProp = accessToken.getProperty(RESOURCE);
        assertThat(resourceProp.isValid()).isFalse();
        assertThat(resourceProp.getFirstFailure().getMessage()).isEqualTo(ERR_RESOURCE_MISMATCH.formatted(OTHER_RESOURCE, CODE_RESOURCE));
        assertThat(accessToken.getResource()).isNull();
    }

    @Test
    public void resource_assigned_before_the_authorisation_code_is_validated_once_the_code_is_assigned() {
        final var code = saveRedeemedCode("CODE_3", CODE_RESOURCE);
        assertThat(code.getResource()).isNotEqualTo(OTHER_RESOURCE);
        final McpAccessToken accessToken = new_(McpAccessToken.class);
        final var resourceProp = accessToken.getProperty(RESOURCE);

        accessToken.setResource(OTHER_RESOURCE);

        assertThat(resourceProp.isValid()).isTrue();
        assertThat(accessToken.getResource()).isEqualTo(OTHER_RESOURCE);

        accessToken.setAuthorisationCode(code);
        assertThat(accessToken.getAuthorisationCode()).isEqualTo(code);

        assertThat(resourceProp.isValid()).isFalse();
        assertThat(resourceProp.getFirstFailure().getMessage()).isEqualTo(ERR_RESOURCE_MISMATCH.formatted(OTHER_RESOURCE, CODE_RESOURCE));
    }

    @Test
    public void resource_of_a_retrieved_access_token_is_validated_against_its_authorisation_code() {
        final var code = saveRedeemedCode("CODE_4", CODE_RESOURCE);
        assertThat(code.getResource()).isEqualTo(CODE_RESOURCE);
        final McpAccessTokenCo co = co$(McpAccessToken.class);
        save(co.new_()
               .setTokenHash(co.tokenHash("TOKEN_4"))
               .setExpirationDate(date("2026-10-24 10:00:00"))
               .setAuthorisationCode(code)
               .setResource(CODE_RESOURCE));
        final var accessToken = co.findByKeyAndFetch(McpAccessTokenCo.FETCH_PROVIDER.fetchModel(), co.tokenHash("TOKEN_4"));
        assertThat(accessToken.getResource()).isEqualTo(CODE_RESOURCE);

        accessToken.setResource(OTHER_RESOURCE);

        final var resourceProp = accessToken.getProperty(RESOURCE);
        assertThat(resourceProp.isValid()).isFalse();
        assertThat(resourceProp.getFirstFailure().getMessage()).isEqualTo(ERR_RESOURCE_MISMATCH.formatted(OTHER_RESOURCE, CODE_RESOURCE));
        assertThat(accessToken.getResource()).isEqualTo(CODE_RESOURCE);
    }

    /// Saves a redeemed, unexpired authorisation code, granted by the current user for `resource`.
    ///
    private McpAuthorisationCode saveRedeemedCode(final String code, final String resource) {
        final var savedCode = save(new_composite(McpAuthorisationCode.class, code)
                .setExpirationDate(date("2026-10-21 10:10:00"))
                .setUser(getUser())
                .setClientId("test_client")
                .setCodeChallenge("A".repeat(43))
                .setRedirectUri("http://localhost/callback")
                .setResource(resource));
        return save(savedCode.setActive(false));
    }

}
