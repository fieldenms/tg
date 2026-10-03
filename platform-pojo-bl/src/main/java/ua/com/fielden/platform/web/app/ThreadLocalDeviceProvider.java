package ua.com.fielden.platform.web.app;

import jakarta.inject.Singleton;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;

/**
 * This is a thread-safe implementation of {@link IDeviceProvider} that simply holds a {@link DeviceProfile} value that is set via setter in a {@link ThreadLocal} variable.
 * 
 * @author TG Team
 * 
 */
@Singleton
public class ThreadLocalDeviceProvider implements IDeviceProvider {
    
    private final ThreadLocal<DeviceProfile> deviceProfile = new ThreadLocal<>();
    
    @Override
    public DeviceProfile getDeviceProfile() {
        return deviceProfile.get();
    }
    
    @Override
    public void setDeviceProfile(final DeviceProfile deviceProfile) {
        this.deviceProfile.set(deviceProfile);
    }
    
}
