package ua.com.fielden.platform.web.interfaces;

import static ua.com.fielden.platform.web.interfaces.DeviceProfile.MOBILE;

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

    /// Determines whether the current device profile is [DeviceProfile#MOBILE].
    ///
    /// Any other value counts as [DeviceProfile#DESKTOP], including `null`, which means that no request is being served.
    /// `AbstractWebResource.calculateDeviceProfile` likewise resolves a request without a recognised profile to `DESKTOP`.
    /// Code choosing between desktop and mobile behaviour should use this method, so that all such code agrees.
    ///
    default boolean isMobile() {
        return getDeviceProfile() == MOBILE;
    }

    /// Clears the device profile from this provider.
    ///
    /// Implementations backed by thread-confined storage (e.g. a [ThreadLocal]) must remove the stored value.
    /// Otherwise, a worker thread returned to a pool would retain a previous request's device profile.
    /// This should be invoked once a unit of work (such as an HTTP request) completes.
    ///
    void clearDeviceProfile();

}
