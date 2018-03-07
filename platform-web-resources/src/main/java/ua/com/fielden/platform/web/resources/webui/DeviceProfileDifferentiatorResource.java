package ua.com.fielden.platform.web.resources.webui;

import static ua.com.fielden.platform.web.interfaces.DeviceProfile.DESKTOP;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.MOBILE;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;

/**
 * A base resource implementation for those resources that need to have specific handling for different {@link DeviceProfile}s.
 *
 * @author TG Team
 *
 */
public class DeviceProfileDifferentiatorResource extends ServerResource {
    private final DeviceProfile deviceProfile;
    
    public DeviceProfileDifferentiatorResource(final Context context, final Request request, final Response response, final IUserProvider userProvider) {
        init(context, request, response);
        userProvider.setDeviceProfile(deviceProfile = calculateDeviceProfile(request));
    }
    
    /**
     * Calculates the {@link DeviceProfile} that is relevant to the specified <code>request</code>.
     *
     * @param request
     * @return
     */
    private static DeviceProfile calculateDeviceProfile(final Request request) {
        // It is recommended to use word "Mobi" for mobile device detection, see https://developer.mozilla.org/en-US/docs/Web/HTTP/Browser_detection_using_the_user_agent for more info.
        // At this stage "Tablet" token will be skipped for 'Mozilla (Gecko, Firefox)' browsers -- this will direct the page to the desktop version.
        if (request.getClientInfo().getAgent().contains("Mobi")) {
            return MOBILE;
        } else {
            return DESKTOP;
        }
    }
    
    /**
     * Returns the {@link DeviceProfile} that is associated with this source request.
     *
     * @return
     */
    protected DeviceProfile device() {
        return deviceProfile;
    }
    
}