package ua.com.fielden.platform.security.user.impl;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;

import com.google.inject.Inject;

import jakarta.inject.Singleton;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.basic.config.IApplicationSettings.AuthMode;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.IAuthenticationModel;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserSecret;
import ua.com.fielden.platform.security.user.UserSecretCo;

/**
 * An authentication model for explicit user logins (thread-safe).
 * It supports both RSO and SSO modes, whereby in the SSO mode users are checked if they are permitted to login with RSO.
 *
 * @author TG Team
 *
 */
@Singleton
public class DefaultAuthenticationModel implements IAuthenticationModel {

    private static final Result failedAuthResult = failure("The presented login credentials are not recognized.");
    private final ICompanionObjectFinder coFinder;
    private final AuthMode authMode;

    @Inject
    public DefaultAuthenticationModel(final ICompanionObjectFinder coFinder, final IApplicationSettings settings) {
        this.coFinder = coFinder;
        this.authMode = settings.authMode();
    }

    @Override
    public Result authenticate(final String username, final String password) {
        try {
            // check attempts to login in with UNIT_TEST_USER and fail those
            if (User.system_users.UNIT_TEST_USER.matches(username)) {
                return failedAuthResult;
            }
            final UserSecretCo coUserSecret = coFinder.find(UserSecret.class, true);
            // in the SSO mode, users should only be able to login with RSO if they are permitted (i.e., their ssOnly property is false).
            final ICompoundCondition0<UserSecret> rsoCondition = select(UserSecret.class).where().lowerCase().prop("key.key").eq().lowerCase().val(username).and().prop("key.active").eq().val(true);
            final EntityResultQueryModel<UserSecret> query = (authMode == AuthMode.RSO ? rsoCondition : rsoCondition.and().prop("key.ssoOnly").eq().val(false)).model();
            final fetch<UserSecret> fetch = coUserSecret.getFetchProvider().fetchModel();
            final QueryExecutionModel<UserSecret, EntityResultQueryModel<UserSecret>> qem = from(query).with(fetch).model();

            return coUserSecret.getEntityOptional(qem)
                    .map(secret -> equalsEx(secret.getPassword(), coUserSecret.hashPasswd(password, secret.getSalt())) ? successful(secret.getKey()) : failedAuthResult)
                    .orElseGet(() -> {
                        // let's mimic password hashing for unrecognised users to protected from timing-based attacks that would allow enumerating valid user names
                        coUserSecret.hashPasswd(password, coUserSecret.newSalt());
                        // the original result needs to be returned.
                        return failedAuthResult;
                    });
            
        } catch (final Exception ex) {
            return failure(ex);
        }
    }

}
