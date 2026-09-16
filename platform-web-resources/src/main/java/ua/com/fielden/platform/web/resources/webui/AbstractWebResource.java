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

/// An abstract resource implementation for TG web resources (except login / logout resources and attachment download resource).
///
/// Resolves the [DeviceProfile] of each request from the [#DEVICE_PROFILE_HEADER] header sent by the client,
/// and exposes it via [#device()] to those resources that need specific handling for different [DeviceProfile]s.
///
/// **WARNING:** every server resource should invoke one of `DefaultDates.setRequestTimeZone` methods to redefine
/// its thread-local request time-zone (potentially empty).
/// This is because threads can be reused for different resources and, if not redefined, a previous request
/// time-zone will be taken, potentially from another user and another time-zone.
/// For [AbstractWebResource] descendants this is done automatically.
///
/// Keep in mind that the client must send a `Time-Zone` header with a tz database time-zone ID for such resources.
/// It sends [#DEVICE_PROFILE_HEADER] alongside it, from the same helper.
///
public abstract class AbstractWebResource extends ServerResource {

    /// Name of the request header carrying the [DeviceProfile] resolved by the client.
    /// Client-side counterpart is `DEVICE_PROFILE_HEADER` in `tg-polymer-utils.js`.
    ///
    public static final String DEVICE_PROFILE_HEADER = "Device-Profile";

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

    /// Calculates the [DeviceProfile] that is relevant to the specified `request`.
    ///
    /// The client is the single source of truth: it resolves the profile from hardware capabilities and sends it in
    /// the [#DEVICE_PROFILE_HEADER] header together with `Time-Zone` (see `deviceProfile` in `tg-polymer-utils.js`
    /// and `_requestHeaders` in `tg-request-headers.js`).
    /// No server-side detection is attempted, so the client and the server cannot arrive at different answers for
    /// the same session.
    ///
    /// Requests without the header are those not issued by the client application itself -- the generated index
    /// resource, the login page, static resources and Service Worker requests.
    /// None of them is profile-specific, so [DeviceProfile#DESKTOP] is returned for them.
    ///
    /// @return the profile named by [#DEVICE_PROFILE_HEADER], or [DeviceProfile#DESKTOP] if the header is absent or unrecognised.
    ///
    private static DeviceProfile calculateDeviceProfile(final Request request) {
        final String profile = request.getHeaders().getFirstValue(DEVICE_PROFILE_HEADER, /*ignore case*/ true);
        return MOBILE.name().equals(profile) ? MOBILE : DESKTOP;
    }

    /// Returns the [DeviceProfile] that is associated with this source request.
    ///
    /// @return the profile resolved once for this request in the constructor.
    ///
    protected DeviceProfile device() {
        return deviceProfile;
    }

}