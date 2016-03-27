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
        String hashPasswd;
        try {
            hashPasswd = coUser.hashPasswd(password);
        } catch (Exception e) {
            throw Result.failure(e);
        }
        
        final EntityResultQueryModel<User> matchUserQuery = select(User.class).where()
                .prop("key").eq().val(username)
                .and().prop("password").eq().allOfValues(hashPasswd) 
                .model();
        final User user = coUser.getEntity(from(matchUserQuery).with(fetchAll(User.class)).model());

        if (user != null) {
            return Result.successful(user);
        }
        
        return Result.failure("The presented login credentials are not recognized.");
    }

}
