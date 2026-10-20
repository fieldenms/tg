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

    /// Clears the device profile from this provider.
    ///
    /// Implementations backed by thread-confined storage (e.g. a [ThreadLocal]) must remove the stored value.
    /// Otherwise, a worker thread returned to a pool would retain a previous request's device profile.
    /// This should be invoked once a unit of work (such as an HTTP request) completes.
    ///
    void clearDeviceProfile();

}
