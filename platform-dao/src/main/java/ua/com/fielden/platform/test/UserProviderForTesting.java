package ua.com.fielden.platform.test;

import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;

/**
 * Provider for testing purposes.
 * 
 * @author TG Team
 * 
 */
public class UserProviderForTesting implements IUserProvider {

    private User user = new User("TEST-USER", "test user") {
    };

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setUsername(final String username, final IUserController controller) {
        user = new User(username, "test user") {
        };
    }

}
