package ua.com.fielden.platform.security;

import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.security.SecurityTokenInfoUtils.shortDesc;
import static ua.com.fielden.platform.security.user.User.system_users.VIRTUAL_USER;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.provider.ISecurityTokenController;
import ua.com.fielden.platform.security.user.IUserProvider;

/**
 * Server authorisation model, which controls access to methods with annotation {@link Authorise}.
 *
 * @author TG Team
 *
 */
@Singleton
public class ServerAuthorisationModel extends AbstractAuthorisationModel {

    private static final Logger LOGGER = LogManager.getLogger(ServerAuthorisationModel.class);
    protected final ISecurityTokenController controller;
    private final IUserProvider userProvider;

    @Inject
    public ServerAuthorisationModel(final ISecurityTokenController controller, final IUserProvider userProvider) {
        this.controller = controller;
        this.userProvider = userProvider;
    }

    @Override
    public Result authorise(final Class<? extends ISecurityToken> token) {
        final var currUser = userProvider.getUser();
        final var authorised = VIRTUAL_USER.matches(currUser) ||
                               controller.canAccess(currUser, token);
        if (authorised) {
            return successful();
        } else {
            final var msg = "Permission denied due to token [%s] restriction.".formatted(shortDesc(token));
            LOGGER.warn(() -> "[%s] %s".formatted(currUser, msg));
            return failure(msg);
        }
    }

}
