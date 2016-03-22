package ua.com.fielden.platform.web.resources.webui;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.web.app.ISourceController;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * A base resource implementation for those resources that need to have specific handling for different {@link DeviceProfile}s.
 *
 * @author TG Team
 *
 */
public class DeviceProfileDifferentiatorResource extends ServerResource {
    private final DeviceProfile deviceProfile;
    private final ISourceController sourceController;
    private final RestServerUtil restUtil;

    public DeviceProfileDifferentiatorResource(final ISourceController sourceController, final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
        init(context, request, response);
        this.sourceController = sourceController;
        this.restUtil = restUtil;
        this.deviceProfile = calculateDeviceProfile(request);
    }

    /**
     * Calculates the {@link DeviceProfile} that is relevant to the specified <code>request</code>.
     *
     * @param request
     * @return
     */
    private DeviceProfile calculateDeviceProfile(final Request request) {
        // browsers from mobile phones also send word Mobile as part of the agent info
        // however, both Android tablets and mobiles send work Android.
        // therefore, in case there would be a need to distinguish between tablets and mobiles the following condition would need to be enhanced
        // also, there was no testing done for iOS devices... Chrom on iOS would include word CriOS, but that is different for Safari...
        if (request.getClientInfo().getAgent().contains("Android") || request.getClientInfo().getAgent().contains("CriOS")) {
            return DeviceProfile.MOBILE;
        } else {
            return DeviceProfile.DESKTOP;
        }
    }

    /**
     * Returns the {@link DeviceProfile} that is associated with this source request.
     *
     * @return
     */
    protected DeviceProfile deviceProfile() {
        return deviceProfile;
    }

    /**
     * Returns the {@link ISourceController} instance.
     *
     * @return
     */
    protected ISourceController sourceController() {
        return sourceController;
    }

    /**
     * Returns the {@link RestServerUtil} instance.
     *
     * @return
     */
    protected RestServerUtil restUtil() {
        return restUtil;
    }
}
