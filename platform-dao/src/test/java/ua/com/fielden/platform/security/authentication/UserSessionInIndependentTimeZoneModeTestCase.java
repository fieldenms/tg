package ua.com.fielden.platform.security.authentication;

import com.google.common.cache.Cache;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.session.UserSessionDao;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.IUniversalConstants;

import static java.util.concurrent.TimeUnit.HOURS;
import static org.joda.time.DateTimeZone.getDefault;
import static org.junit.Assert.*;

/// A test case to cover user sessions in the independent time-zone mode.
///
/// The independent time-zone mode is modelled with a request time zone ahead of, or behind, the server time zone, which shifts `now()`, but not `serverNow()`.
/// Sessions expire by the server clock, so that their duration does not depend on the request time zone of the requests that establish, validate or clear them.
/// The sessions in this test case are untrusted, which expire after 5 minutes in the test configuration.
///
public class UserSessionInIndependentTimeZoneModeTestCase extends AbstractDaoTestCase {

    /// The offset of the modelled request time zones from the server time zone.
    ///
    private static final int REQUEST_TIME_ZONE_OFFSET_IN_HOURS = 5;
    private static final int UNTRUSTED_SESSION_DURATION_IN_MINUTES = 5;

    private final UserSessionDao coSession = (UserSessionDao) co$(UserSession.class);
    private final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
    private final Cache<String, UserSession> cache = coSession.getCache();

    @Before
    public void startUp() {
        // Due to the global nature of the cache, it needs to be invalidated to keep tests independent.
        cache.invalidateAll();
        constants.setNow(dateTime("2015-04-23 13:00:00"));
    }

    @After
    public void removeRequestTimeZone() {
        constants.removeRequestTimeZone();
    }

    @Test
    public void session_established_from_a_time_zone_ahead_of_the_server_expires_by_the_server_clock() {
        assertSessionExpiresByTheServerClock(requestTimeZone(REQUEST_TIME_ZONE_OFFSET_IN_HOURS));
    }

    @Test
    public void session_established_from_a_time_zone_behind_the_server_expires_by_the_server_clock() {
        assertSessionExpiresByTheServerClock(requestTimeZone(-REQUEST_TIME_ZONE_OFFSET_IN_HOURS));
    }

    @Test
    public void session_replaced_during_a_request_from_a_time_zone_ahead_of_the_server_is_prolonged_by_the_server_clock() {
        final User currUser = currUser();
        final UserSession oldSession = coSession.newSession(currUser, false, null);

        constants.setRequestTimeZone(requestTimeZone(REQUEST_TIME_ZONE_OFFSET_IN_HOURS));
        constants.setNow(dateTime("2015-04-23 13:02:00"));
        assertRequestTimeZoneShiftsNow();
        final UserSession newSession = coSession.currentSession(currUser, oldSession.getAuthenticator().orElseThrow().toString(), false).orElseThrow();

        // Both the new session and the replaced one, which remains valid for concurrent requests with its authenticator, expire at 13:07 by the server clock.
        final DateTime expiryTime = dateTime("2015-04-23 13:07:00");
        assertNotEquals(oldSession.getId(), newSession.getId());
        assertEquals(expiryTime.toDate(), newSession.getExpiryTime());
        assertEquals(expiryTime.toDate(), coSession.findById(oldSession.getId()).getExpiryTime());
    }

    @Test
    public void clearExpired_removes_only_sessions_expired_by_the_server_clock() {
        assertClearsOnlySessionsExpiredByTheServerClock(coSession::clearExpired);
    }

    @Test
    public void clearExpired_for_a_user_removes_only_sessions_expired_by_the_server_clock() {
        assertClearsOnlySessionsExpiredByTheServerClock(() -> coSession.clearExpired(currUser()));
    }

    /// Establishes a session during a request from `requestTimeZone`, and asserts that it expires after [#UNTRUSTED_SESSION_DURATION_IN_MINUTES] by the server clock.
    ///
    private void assertSessionExpiresByTheServerClock(final DateTimeZone requestTimeZone) {
        final DateTime serverTimeOfRequest = constants.serverNow();
        constants.setRequestTimeZone(requestTimeZone);
        assertRequestTimeZoneShiftsNow();

        final User currUser = currUser();
        final UserSession newSession = coSession.newSession(currUser, false, null);
        final DateTime expiryTime = serverTimeOfRequest.plusMinutes(UNTRUSTED_SESSION_DURATION_IN_MINUTES);
        assertEquals(expiryTime.toDate(), newSession.getExpiryTime());
        assertEquals(serverTimeOfRequest.toDate(), newSession.getLastAccess());

        final String authenticator = newSession.getAuthenticator().orElseThrow().toString();
        constants.setNow(expiryTime.minusSeconds(1));
        assertTrue(coSession.currentSession(currUser, authenticator, true).isPresent());
        constants.setNow(expiryTime.plusSeconds(1));
        assertFalse(coSession.currentSession(currUser, authenticator, true).isPresent());
    }

    /// Establishes a session, and asserts that `clear` does not remove it before it expires by the server clock during a request from a time zone ahead of the server,
    /// and removes it after it expires by the server clock during a request from a time zone behind the server.
    ///
    private void assertClearsOnlySessionsExpiredByTheServerClock(final Runnable clear) {
        final UserSession session = coSession.newSession(currUser(), false, null);
        final DateTime expiryTime = dateTime("2015-04-23 13:05:00");

        constants.setRequestTimeZone(requestTimeZone(REQUEST_TIME_ZONE_OFFSET_IN_HOURS));
        constants.setNow(expiryTime.minusSeconds(1));
        assertRequestTimeZoneShiftsNow();
        clear.run();
        assertTrue(coSession.findByIdOptional(session.getId()).isPresent());

        constants.setRequestTimeZone(requestTimeZone(-REQUEST_TIME_ZONE_OFFSET_IN_HOURS));
        constants.setNow(expiryTime.plusSeconds(1));
        assertRequestTimeZoneShiftsNow();
        clear.run();
        assertTrue(coSession.findByIdOptional(session.getId()).isEmpty());
    }

    /// Asserts that the modelled request time zone shifts `now()` from `serverNow()`, which these tests rely on to tell them apart.
    ///
    private void assertRequestTimeZoneShiftsNow() {
        assertNotEquals("Invalid initial conditions: the request time zone should shift 'now()'.", constants.serverNow().getMillis(), constants.now().getMillis());
    }

    /// A time zone with a fixed offset of `offsetInHours` from the server time zone at the modelled time.
    ///
    private DateTimeZone requestTimeZone(final int offsetInHours) {
        return DateTimeZone.forOffsetMillis(getDefault().getOffset(constants.serverNow()) + (int) HOURS.toMillis(offsetInHours));
    }

    private User currUser() {
        return getInstance(IUserProvider.class).getUser();
    }

}
