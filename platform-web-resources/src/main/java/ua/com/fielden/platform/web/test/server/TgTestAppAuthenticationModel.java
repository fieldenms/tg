package ua.com.fielden.platform.web.test.server;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.provider.IUserEx;
import ua.com.fielden.platform.security.user.IAuthenticationModel;
import ua.com.fielden.platform.security.user.User;

import com.google.inject.Inject;

/**
 * This is an authentication model for the TG test application.
 * It it extremely simplistic, just makes sure that there is a user with the specified <code>username</code>.
 *
 * @author TG Team
 *
 */
public class TgTestAppAuthenticationModel implements IAuthenticationModel {

    private final IUserEx coUserEx;

    @Inject
    public TgTestAppAuthenticationModel(final IUserEx coUserEx) {
        this.coUserEx = coUserEx;
    }

    @Override
    public Result authenticate(final String username, final String password) {
        final User user = coUserEx.findByKeyAndFetch(fetch(User.class).with("key").with("password"), username);
        if (user != null) {
            return Result.successful(user);
        }
        return Result.failure("User did not pass authentication.");
    }

}
