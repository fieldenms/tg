package ua.com.fielden.platform.security.user.impl;

import ua.com.fielden.platform.security.provider.IUserEx;
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

    public void setUsername(final String username, final IUserEx controller) {
        final User user = controller.findUser(username);
        if (user == null) {
            throw new IllegalArgumentException("Could not find user '" + username + "'.");
        }
        this.users.set(user);
    }

}
