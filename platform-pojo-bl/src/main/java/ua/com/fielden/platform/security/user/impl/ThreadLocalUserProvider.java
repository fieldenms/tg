package ua.com.fielden.platform.security.user.impl;

import static java.lang.String.format;

import com.google.inject.Singleton;

import ua.com.fielden.platform.security.exceptions.SecurityException;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;

/**
 * This is a thread-safe implementation of {@link IUserProvider} that simply holds a user value that is set via setter in a {@link ThreadLocal} variable.
 * 
 * @author TG Team
 * 
 */
@Singleton
public class ThreadLocalUserProvider implements IUserProvider {

    private final ThreadLocal<User> users = new ThreadLocal<>();

    @Override
    public User getUser() {
        return users.get();
    }

    @Override
    public IUserProvider setUsername(final String username, final IUser coUser) {
        final User user = coUser.findUser(username);
        if (user == null) {
            throw new SecurityException(format("Could not find user [%s].", username));
        }
        this.users.set(user);
        return this;
    }

    @Override
    public IUserProvider setUser(final User user) {
        this.users.set(user);
        return this;
    }

}
