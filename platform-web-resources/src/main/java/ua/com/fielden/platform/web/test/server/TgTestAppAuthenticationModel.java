package ua.com.fielden.platform.web.test.server;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.IAuthenticationModel;
import ua.com.fielden.platform.security.user.IUserSecret;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserSecret;

/**
 * This is an authentication model for the TG test application.
 * It it extremely simplistic, just makes sure that there is a user with the specified <code>username</code>.
 *
 * @author TG Team
 *
 */
public class TgTestAppAuthenticationModel implements IAuthenticationModel {

    private final IUserSecret coUserSecret;

    @Inject
    public TgTestAppAuthenticationModel(final IUserSecret coUserSecret) {
        this.coUserSecret = coUserSecret;
    }

    @Override
    public Result authenticate(final String username, final String password) {
        try {
            final Result result = failure("The presented login credentials are not recognized.");
            // check attempts to login in with UNIT_TEST_USER and fail those
            if (User.system_users.UNIT_TEST_USER.matches(username)) {
                return result;
            }

            final EntityResultQueryModel<UserSecret> query = select(UserSecret.class).where().prop("key.key").eq().val(username).and().prop("key.active").eq().val(true).model();
            final fetch<UserSecret> fetch = coUserSecret.getFetchProvider().fetchModel();
            final QueryExecutionModel<UserSecret, EntityResultQueryModel<UserSecret>> qem = from(query).with(fetch).model();
            
            return coUserSecret.getEntityOptional(qem)
                    .map(secret -> equalsEx(secret.getPassword(), coUserSecret.hashPasswd(password, secret.getSalt())) ? successful(secret.getKey()) : result)
                    .orElse(result);
            
        } catch (final Exception ex) {
            return failure(ex);
        }
    }

}
