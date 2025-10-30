package ua.com.fielden.platform.web.resources.webui;

import static ua.com.fielden.platform.web.interfaces.DeviceProfile.DESKTOP;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.MOBILE;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.utils.DefaultDates;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;

/**
 * An abstract resource implementation for TG web resources (except login / logout resources and attachment download resource).
 * Contains device profile differentiation logic that can be used in those resources that need specific handling for different {@link DeviceProfile}s.
 * <p>
 * WARNING: every server resource should invoke one of {@code DefaultDates.setRequestTimeZone} methods to redefine its thread-local request time-zone (potentially empty);<br>
 *          this is because threads can be reused for different resources and, if not redefined, previous request time-zone will be taken, potentially from other user and other time-zone;<br>
 *          for {@link AbstractWebResource} descendants this will be done automatically;<br>
 *          keep in mind that client must send 'Time-Zone' header with tz database time-zone ID for such resources
 *
 * @author TG Team
 *
 */
public abstract class AbstractWebResource extends ServerResource {
    private final DeviceProfile deviceProfile;
    protected final IDates dates;

    public AbstractWebResource(final Context context, final Request request, final Response response, final IDeviceProvider deviceProvider, final IDates dates) {
        init(context, request, response);
        deviceProvider.setDeviceProfile(deviceProfile = calculateDeviceProfile(request));
        if (dates instanceof DefaultDates) {
            ((DefaultDates) dates).setRequestTimeZone(request.getHeaders().getValues("Time-Zone"));
        }
        this.dates = dates;
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
        final String agent = request.getClientInfo().getAgent();
        if (agent != null && agent.contains("Mobi")) {
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