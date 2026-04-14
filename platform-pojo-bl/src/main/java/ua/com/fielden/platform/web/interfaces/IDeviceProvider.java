package ua.com.fielden.platform.web.interfaces;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.web.app.ThreadLocalDeviceProvider;

/**
 * Interface interacting with current {@link DeviceProfile}.
 * Used internally in serialisation / criteria entity restoration / server resources logic; distinguishes requests 
 * from different client applications.
 * 
 * @author TG Team
 *
 */
@ImplementedBy(ThreadLocalDeviceProvider.class)
public interface IDeviceProvider {
    
    DeviceProfile getDeviceProfile();
    void setDeviceProfile(final DeviceProfile deviceProfile);
    
}
