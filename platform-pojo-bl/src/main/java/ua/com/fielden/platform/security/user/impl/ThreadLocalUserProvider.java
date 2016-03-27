package ua.com.fielden.platform.security.user.impl;

import static java.lang.String.format;

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
public class ThreadLocalUserProvider implements IUserProvider {

    public ThreadLocal<User> users = new ThreadLocal<>();

    @Override
    public User getUser() {
        return users.get();
    }

    public void setUsername(final String username, final IUser coUser) {
        final User user = coUser.findUser(username);
        if (user == null) {
            throw new SecurityException(format("Could not find user [%s].", username));
        }
        this.users.set(user);
    }

}
