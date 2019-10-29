package ua.com.fielden.platform.web.interfaces;

/**
 * Interface interacting with current {@link DeviceProfile}.
 * Used internally in serialisation / criteria entity restoration / server resources logic; distinguishes requests 
 * from different client applications.
 * 
 * @author TG Team
 *
 */
public interface IDeviceProvider {
    
    DeviceProfile getDeviceProfile();
    void setDeviceProfile(final DeviceProfile deviceProfile);
    
}
