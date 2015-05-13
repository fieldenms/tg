package ua.com.fielden.platform.web.security;

import org.restlet.Context;

import ua.com.fielden.platform.security.provider.IUserEx;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;

import com.google.inject.Injector;

/**
 * The default implementation of {@link AbstractWebResourceGuard}, which should be applicable in all foreseeable scenarios.
 * <p>
 * It's method {@link #getUser(String)} not only retrieves and returns the current user, but also sets it for the current thread by updating the relevant
 * instance of {@link IUserProvider}.
 * Due to the fact that all web resources that require a valid user to be accessed are guarded, they can obtain the current user by simply calling method {@link IUserProvider#getUser()}.
 *
 * @author TG Team
 *
 */
public class DefaultWebResourceGuard extends AbstractWebResourceGuard {

    public DefaultWebResourceGuard(final Context context, final Injector injector) throws IllegalArgumentException {
        super(context, injector);
    }

    @Override
    protected User getUser(final String username) {
        final IUserEx coUserEx = injector.getInstance(IUserEx.class);
        final IUserProvider up = injector.getInstance(IUserProvider.class);
        up.setUsername(username, coUserEx);
        return up.getUser();
    }

}
