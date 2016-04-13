package ua.com.fielden.platform.web.test.server;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.IAuthenticationModel;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;

/**
 * This is an authentication model for the TG test application.
 * It it extremely simplistic, just makes sure that there is a user with the specified <code>username</code>.
 *
 * @author TG Team
 *
 */
public class TgTestAppAuthenticationModel implements IAuthenticationModel {

    private final IUser coUser;

    @Inject
    public TgTestAppAuthenticationModel(final IUser coUser) {
        this.coUser = coUser;
    }

    @Override
    public Result authenticate(final String username, final String password) {
        try {
            final Result result = Result.failure("The presented login credentials are not recognized.");
            // check attempts to login in with UNIT_TEST_USER and fail those
            if (User.system_users.UNIT_TEST_USER.matches(username)) {
                return result;
            }
            
            final User user = coUser.findByKeyAndFetch(fetchAll(User.class), username);
            if (user != null) {
                final String hashPasswd = coUser.hashPasswd(password, user.getSalt());
                if (!hashPasswd.equals(user.getPassword())) {
                    return result;
                }
            
            } else {
                return result;
            }
            return Result.successful(user);
        } catch (final Exception ex) {
            return Result.failure(ex);
        }
    }

}
