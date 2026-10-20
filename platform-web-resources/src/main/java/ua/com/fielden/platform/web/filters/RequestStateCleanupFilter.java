package ua.com.fielden.platform.web.filters;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.routing.Filter;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.utils.DefaultDates;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;

/// A filter that clears request-specific values held in thread-locals of the current thread.
///
/// Jetty/Restlet reuse worker threads across requests, and request-specific values are held in thread-locals.
/// They hold the user ([IUserProvider]), device profile ([IDeviceProvider]) and time-zone ([DefaultDates]).
/// It wraps the whole application chain, so that they are always cleared once a request has been fully handled.
/// This holds even when handling throws.
/// This prevents a pooled thread from carrying a previous request's values into subsequent work that reads them.
///
/// The user is set during authentication by the guard; the other two are set later, by `AbstractWebResource`.
/// The device profile and the time-zone are also cleared before handling, as authentication relies on `now()`.
/// Otherwise, it could see a time-zone left by a resource outside this chain, such as a Web API resource.
///
public class RequestStateCleanupFilter extends Filter {

    private final IUserProvider userProvider;
    private final IDeviceProvider deviceProvider;
    private final IDates dates;

    public RequestStateCleanupFilter(
        final Context context,
        final Restlet next,
        final IUserProvider userProvider,
        final IDeviceProvider deviceProvider,
        final IDates dates
    ) {
        super(context, next);
        this.userProvider = userProvider;
        this.deviceProvider = deviceProvider;
        this.dates = dates;
    }

    @Override
    protected int doHandle(final Request request, final Response response) {
        clearDeviceProfileAndTimeZone();
        try {
            return super.doHandle(request, response);
        } finally {
            userProvider.clearUser();
            clearDeviceProfileAndTimeZone();
        }
    }

    /// Clears the device profile and the request time-zone, which `AbstractWebResource` sets on the current thread.
    ///
    private void clearDeviceProfileAndTimeZone() {
        deviceProvider.clearDeviceProfile();
        if (dates instanceof DefaultDates defaultDates) {
            defaultDates.clearRequestTimeZone();
        }
    }

}
