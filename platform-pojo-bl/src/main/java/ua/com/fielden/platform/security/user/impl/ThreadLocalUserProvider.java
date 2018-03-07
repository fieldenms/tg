package ua.com.fielden.platform.security.user.impl;

import static java.lang.String.format;

import ua.com.fielden.platform.security.exceptions.SecurityException;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;

/**
 * This is a thread-safe implementation of {@link IUserProvider} that simply holds a user value that is set via setter in a {@link ThreadLocal} variable.
 * 
 * @author TG Team
 * 
 */
public class ThreadLocalUserProvider implements IUserProvider {
    
    private final ThreadLocal<User> users = new ThreadLocal<>();
    private final ThreadLocal<DeviceProfile> deviceProfile = new ThreadLocal<>();
    
    @Override
    public User getUser() {
        return users.get();
    }
    
    @Override
    public void setUsername(final String username, final IUser coUser) {
        final User user = coUser.findUser(username);
        if (user == null) {
            throw new SecurityException(format("Could not find user [%s].", username));
        }
        this.users.set(user);
    }
    
    @Override
    public void setUser(final User user) {
        this.users.set(user);
    }
    
    @Override
    public DeviceProfile getDeviceProfile() {
        return deviceProfile.get();
    }
    
    @Override
    public void setDeviceProfile(final DeviceProfile deviceProfile) {
        this.deviceProfile.set(deviceProfile);
    }
    
}
