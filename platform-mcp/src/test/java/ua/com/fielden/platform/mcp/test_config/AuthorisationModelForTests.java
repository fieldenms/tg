package ua.com.fielden.platform.mcp.test_config;

import jakarta.inject.Inject;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.AbstractAuthorisationModel;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.ServerAuthorisationModel;
import ua.com.fielden.platform.security.user.IUserProvider;

import static ua.com.fielden.platform.error.Result.successful;

/// Authorisation model for MCP server tests.
/// Grants universal access to the common test user [AbstractTgMcpServerTestCase#USER_MCP_TEST].
/// For other users, delegates to [ServerAuthorisationModel].
///
class AuthorisationModelForTests extends AbstractAuthorisationModel {

    private final IUserProvider userProvider;
    private final ServerAuthorisationModel serverAuthorisationModel;

    @Inject
    protected AuthorisationModelForTests(
            final IUserProvider userProvider,
            final ServerAuthorisationModel serverAuthorisationModel)
    {
        this.userProvider = userProvider;
        this.serverAuthorisationModel = serverAuthorisationModel;
    }

    @Override
    public Result authorise(final Class<? extends ISecurityToken> token) {
        final var currUser = userProvider.getUser();
        if (currUser != null && currUser.getKey().equals(AbstractTgMcpServerTestCase.USER_MCP_TEST)) {
            return successful();
        }
        else {
            return serverAuthorisationModel.authorise(token);
        }
    }

}
