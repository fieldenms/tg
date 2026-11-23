package ua.com.fielden.platform.web.interfaces;

import com.google.inject.ImplementedBy;
import jakarta.annotation.Nullable;
import ua.com.fielden.platform.web.app.ThreadLocalDeviceProvider;

import static ua.com.fielden.platform.web.interfaces.DeviceProfile.MOBILE;

/// Interface for interacting with the current [DeviceProfile].
/// Used internally in serialisation / criteria entity restoration / server resources logic.
/// Distinguishes requests from different client applications.
///
@ImplementedBy(ThreadLocalDeviceProvider.class)
public interface IDeviceProvider {
    
    @Nullable DeviceProfile getDeviceProfile();

    void setDeviceProfile(DeviceProfile deviceProfile);

    /// Determines whether the current device profile is [DeviceProfile#MOBILE].
    ///
    /// Any other value counts as [DeviceProfile#DESKTOP], including `null`, which means that no request is being served.
    /// `AbstractWebResource.calculateDeviceProfile` likewise resolves a request without a recognised profile to [DeviceProfile#DESKTOP].
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
