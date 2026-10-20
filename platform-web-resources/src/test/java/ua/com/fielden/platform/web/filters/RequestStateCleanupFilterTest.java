package ua.com.fielden.platform.web.filters;

import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.impl.ThreadLocalUserProvider;
import ua.com.fielden.platform.utils.DefaultDates;
import ua.com.fielden.platform.web.app.ThreadLocalDeviceProvider;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Optional.empty;
import static org.junit.Assert.*;
import static org.restlet.data.Method.GET;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.MOBILE;

/// Unit test for [RequestStateCleanupFilter].
///
/// The test thread stands for a pooled worker thread: its thread-locals are kept from one request to the next.
/// Values set on it before a request is handled stand for those left by a previous request.
///
public class RequestStateCleanupFilterTest {

    private final ThreadLocalUserProvider userProvider = new ThreadLocalUserProvider();
    private final ThreadLocalDeviceProvider deviceProvider = new ThreadLocalDeviceProvider();
    private final DefaultDates dates = new DefaultDates(false, 1, 1, 7);

    /// The device profile and time-zone that the next restlet sees on the current thread while handling a request.
    ///
    private record Observed(DeviceProfile deviceProfile, Optional<DateTimeZone> timeZone) {}

    @Test
    public void device_profile_and_time_zone_left_by_a_previous_request_are_cleared_before_handling() {
        setRequestValues();
        assertRequestValuesSet();
        final var observed = new AtomicReference<Observed>();

        handle(restletOf(() -> observed.set(new Observed(deviceProvider.getDeviceProfile(), dates.requestTimeZone()))));

        assertEquals(new Observed(null, empty()), observed.get());
    }

    @Test
    public void user_device_profile_and_time_zone_set_during_handling_are_cleared_after_it() {
        handle(restletOf(this::setRequestValues));

        assertRequestValuesCleared();
    }

    @Test
    public void user_device_profile_and_time_zone_set_during_handling_are_cleared_after_it_even_if_handling_throws() {
        final Runnable failingHandling = () -> {
            setRequestValues();
            throw new IllegalStateException("Handling failed.");
        };

        assertThrows(IllegalStateException.class, () -> handle(restletOf(failingHandling)));
        assertRequestValuesCleared();
    }

    /// Handles a request by [RequestStateCleanupFilter], which passes it on to `next`.
    ///
    private void handle(final Restlet next) {
        final var request = new Request(GET, "http://localhost/");
        new RequestStateCleanupFilter(new Context(), next, userProvider, deviceProvider, dates).handle(request, new Response(request));
    }

    /// Creates a restlet that handles a request by running `action`.
    ///
    private static Restlet restletOf(final Runnable action) {
        return new Restlet() {
            @Override
            public void handle(final Request request, final Response response) {
                action.run();
            }
        };
    }

    /// Sets the user, device profile and time-zone on the current thread, as authentication and `AbstractWebResource` do.
    ///
    private void setRequestValues() {
        userProvider.setUser(new User());
        deviceProvider.setDeviceProfile(MOBILE);
        dates.setRequestTimeZone("Pacific/Auckland");
    }

    private void assertRequestValuesSet() {
        assertNotNull(userProvider.getUser());
        assertEquals(MOBILE, deviceProvider.getDeviceProfile());
        assertTrue(dates.requestTimeZone().isPresent());
    }

    private void assertRequestValuesCleared() {
        assertNull(userProvider.getUser());
        assertNull(deviceProvider.getDeviceProfile());
        assertEquals(empty(), dates.requestTimeZone());
    }

}
