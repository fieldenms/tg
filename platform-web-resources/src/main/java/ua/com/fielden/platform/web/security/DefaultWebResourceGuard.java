package ua.com.fielden.platform.web.security;

import org.restlet.Context;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;

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

    private final ICompanionObjectFinder coFinder;
    private final IUserProvider up;

    public DefaultWebResourceGuard(final Context context, final String domainName, final String path, final Injector injector) {
        super(context, domainName, path, injector);
        coFinder = injector.getInstance(ICompanionObjectFinder.class);
        up = injector.getInstance(IUserProvider.class);
    }

    @Override
    protected User getUser(final String username) {
        final IUser coUser = coFinder.find(User.class, true);
        return up.setUsername(username, coUser).getUser();
    }

}
