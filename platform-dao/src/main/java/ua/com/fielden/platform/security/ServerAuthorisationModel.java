package ua.com.fielden.platform.security;

import static java.lang.String.*;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.provider.ISecurityTokenController;
import ua.com.fielden.platform.security.user.IUserProvider;

import com.google.inject.Inject;

/**
 * Server authorisation model, which controlls access to methods with annotation {@link Authorise}.
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
        return controller.canAccess(userProvider.getUser().getKey(), token) ? Result.successful("Authorised")
                : Result.failure(new IllegalStateException(format("Permission denied due to token %s restriction.", token.getAnnotation(KeyTitle.class).value())));
    }

}
