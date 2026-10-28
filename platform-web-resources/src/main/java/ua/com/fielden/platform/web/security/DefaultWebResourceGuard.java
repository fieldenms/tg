package ua.com.fielden.platform.web.security;

import com.google.inject.Injector;
import org.restlet.Context;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;

/// The default implementation of [AbstractWebResourceGuard], which should be applicable in all foreseeable scenarios.
///
/// Method [#getUser(String)] not only retrieves and returns the current user, but also sets it for the current thread by updating the relevant
/// instance of [IUserProvider].
///
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
