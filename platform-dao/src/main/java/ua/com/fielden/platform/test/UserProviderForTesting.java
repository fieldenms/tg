package ua.com.fielden.platform.test;

import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;

/**
 * Provider for testing purposes.
 * 
 * @author TG Team
 * 
 */
public class UserProviderForTesting implements IUserProvider {

    private User user;

    public UserProviderForTesting() {
        user = new User();
        user.setKey("TEST-USER");
        user.setDesc("test user");
    }
    
    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setUsername(final String username, final IUser coUser) {
        user = new User();
        user.setKey(username);
        user.setDesc("test user");
    }

    @Override
    public void setUser(final User user) {
        this.user = user;
    }

}
