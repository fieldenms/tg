package ua.com.fielden.platform.security;

import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.security.SecurityTokenInfoUtils.shortDesc;

import com.google.inject.Inject;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.provider.ISecurityTokenController;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;

/**
 * Server authorisation model, which controls access to methods with annotation {@link Authorise}.
 *
 * @author TG Team
 *
 */
public class ServerAuthorisationModel extends AbstractAuthorisationModel {

    protected final ISecurityTokenController controller;
    private final IUserProvider userProvider;

    @Inject
    public ServerAuthorisationModel(final ISecurityTokenController controller, final IUserProvider userProvider) {
        this.controller = controller;
        this.userProvider = userProvider;
    }

    @Override
    public Result authorise(final Class<? extends ISecurityToken> token) {
        return User.system_users.VIRTUAL_USER.matches(userProvider.getUser()) ||
               controller.canAccess(userProvider.getUser(), token) ? successful("Authorised")
                : failuref("Permission denied due to token [%s] restriction.", shortDesc(token));
    }

}