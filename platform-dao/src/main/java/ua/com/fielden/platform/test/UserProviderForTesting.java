package ua.com.fielden.platform.test;

import static ua.com.fielden.platform.web.interfaces.DeviceProfile.DESKTOP;

import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;

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
    
    @Override
    public DeviceProfile getDeviceProfile() {
        return DESKTOP;
    }
    
    @Override
    public void setDeviceProfile(final DeviceProfile deviceProfile) {
        // mobile device specific testing is unsupported at this stage
    }
    
}
