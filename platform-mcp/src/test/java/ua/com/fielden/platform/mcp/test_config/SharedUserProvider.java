package ua.com.fielden.platform.mcp.test_config;

import com.google.inject.Singleton;
import ua.com.fielden.platform.security.exceptions.SecurityException;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;

/// An [IUserProvider] implementation that stores the user in a shared volatile field, visible to all threads.
/// This is intended for testing only, where the user is set once per test method and all threads (Restlet, Reactor, etc.) need to see it.
///
@Singleton
class SharedUserProvider implements IUserProvider {

    private volatile User user;

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public IUserProvider setUsername(final String username, final IUser coUser) {
        final User user = coUser.findUser(username);
        if (user == null) {
            throw new SecurityException("Could not find user [%s].".formatted(username));
        }
        this.user = user;
        return this;
    }

    @Override
    public IUserProvider setUser(final User user) {
        this.user = user;
        return this;
    }

    @Override
    public void clearUser() {
        user = null;
    }

}
