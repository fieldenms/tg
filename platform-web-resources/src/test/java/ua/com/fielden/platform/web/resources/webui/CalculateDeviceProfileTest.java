package ua.com.fielden.platform.web.resources.webui;

import org.junit.Test;
import org.restlet.Request;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.DESKTOP;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.MOBILE;
import static ua.com.fielden.platform.web.resources.webui.AbstractWebResource.DEVICE_PROFILE_HEADER;
import static ua.com.fielden.platform.web.resources.webui.AbstractWebResource.calculateDeviceProfile;

/// Unit test for [AbstractWebResource#calculateDeviceProfile].
/// Covers a request without the `Device-Profile` header, recognised values in any case, a header name in another case, and an unrecognised value.
///
public class CalculateDeviceProfileTest {

    /// Resolves the profile of a request that carries `headerValue` in a header named `headerName`, or no such header if `headerValue` is `null`.
    ///
    private static DeviceProfile profileFor(final String headerName, final String headerValue) {
        final Request request = new Request();
        if (headerValue != null) {
            request.getHeaders().add(headerName, headerValue);
        }
        return calculateDeviceProfile(request);
    }

    @Test
    public void request_without_the_header_resolves_to_DESKTOP() {
        assertEquals(DESKTOP, profileFor(DEVICE_PROFILE_HEADER, null));
    }

    @Test
    public void header_value_MOBILE_resolves_to_MOBILE() {
        assertEquals(MOBILE, profileFor(DEVICE_PROFILE_HEADER, "MOBILE"));
    }

    @Test
    public void header_value_is_matched_ignoring_case() {
        assertEquals(MOBILE, profileFor(DEVICE_PROFILE_HEADER, "mobile"));
        assertEquals(MOBILE, profileFor(DEVICE_PROFILE_HEADER, "Mobile"));
    }

    @Test
    public void header_name_is_matched_ignoring_case() {
        assertEquals(MOBILE, profileFor("device-profile", "MOBILE"));
    }

    @Test
    public void header_value_DESKTOP_resolves_to_DESKTOP() {
        assertEquals(DESKTOP, profileFor(DEVICE_PROFILE_HEADER, "DESKTOP"));
    }

    @Test
    public void unrecognised_header_value_resolves_to_DESKTOP() {
        assertEquals(DESKTOP, profileFor(DEVICE_PROFILE_HEADER, "TABLET"));
    }

}
