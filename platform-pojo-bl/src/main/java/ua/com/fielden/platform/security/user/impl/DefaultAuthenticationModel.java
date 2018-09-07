package ua.com.fielden.platform.security.user.impl;

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
 * An authentication model for explicit user logins.
 *
 * @author TG Team
 *
 */
public class DefaultAuthenticationModel implements IAuthenticationModel {

    private static final Result failedAuthResult = failure("The presented login credentials are not recognized.");
    private final IUserSecret coUserSecret;
    private final String protectiveSalt;

    @Inject
    public DefaultAuthenticationModel(final IUserSecret coUserSecret) {
        this.coUserSecret = coUserSecret;
        this.protectiveSalt = coUserSecret.newSalt();
    }

    @Override
    public Result authenticate(final String username, final String password) {
        try {
            // check attempts to login in with UNIT_TEST_USER and fail those
            if (User.system_users.UNIT_TEST_USER.matches(username)) {
                return failedAuthResult;
            }

            final EntityResultQueryModel<UserSecret> query = select(UserSecret.class).where().prop("key.key").eq().val(username).and().prop("key.active").eq().val(true).model();
            final fetch<UserSecret> fetch = coUserSecret.getFetchProvider().fetchModel();
            final QueryExecutionModel<UserSecret, EntityResultQueryModel<UserSecret>> qem = from(query).with(fetch).model();
            
            return coUserSecret.getEntityOptional(qem)
                    .map(secret -> equalsEx(secret.getPassword(), coUserSecret.hashPasswd(password, secret.getSalt())) ? successful(secret.getKey()) : failedAuthResult)
                    .orElseGet(() -> {
                        // let's mimic password hashing for unrecognised users to protected from timing-based attacks that would allow enumerating valid user names
                        coUserSecret.hashPasswd(password, protectiveSalt);
                        // the original result needs to be returned.
                        return failedAuthResult;
                    });
            
        } catch (final Exception ex) {
            return failure(ex);
        }
    }

}
