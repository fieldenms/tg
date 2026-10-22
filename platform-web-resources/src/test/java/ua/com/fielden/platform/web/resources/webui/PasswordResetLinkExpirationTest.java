package ua.com.fielden.platform.web.resources.webui;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserSecret;
import ua.com.fielden.platform.security.user.UserSecretCo;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.web.resources.test.AbstractWebResourceWithDaoTestCase;
import ua.com.fielden.platform.web.resources.webui.LoginInitiateResetResource.PasswordResetLink;

import java.util.Optional;

import static java.util.concurrent.TimeUnit.HOURS;
import static org.joda.time.DateTimeZone.getDefault;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.security.user.UserSecretCo.RESET_UUID_EXPIRATION_IN_MINUTES;
import static ua.com.fielden.platform.web.resources.webui.LoginCompleteResetResource.isResetUuidValid;
import static ua.com.fielden.platform.web.resources.webui.LoginInitiateResetResource.MIN_REUSED_LINK_VALIDITY_IN_MINUTES;
import static ua.com.fielden.platform.web.resources.webui.LoginInitiateResetResource.findOrAssignPasswordResetUuid;

/// Tests for the expiration of password reset links, which [LoginInitiateResetResource] assigns and [LoginCompleteResetResource] validates, in the independent time-zone mode.
///
/// The independent time-zone mode is modelled with a request time zone ahead of, or behind, the server time zone, which shifts `now()`, but not `serverNow()`.
/// Reset links expire by the server clock, so that they remain valid for [UserSecretCo#RESET_UUID_EXPIRATION_IN_MINUTES] regardless of the request time zone.
///
public class PasswordResetLinkExpirationTest extends AbstractWebResourceWithDaoTestCase {

    /// The offset of the modelled request time zones from the server time zone.
    ///
    private static final int REQUEST_TIME_ZONE_OFFSET_IN_HOURS = 5;

    private final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
    private final IDates dates = getInstance(IDates.class);

    @Before
    public void setNow() {
        constants.setNow(dateTime("2026-10-21 10:00:00"));
    }

    @After
    public void removeRequestTimeZone() {
        constants.removeRequestTimeZone();
    }

    @Test
    public void reset_link_requested_from_a_time_zone_ahead_of_the_server_is_valid_for_the_full_period_by_the_server_clock() {
        assertResetLinkIsValidForTheFullPeriod(requestTimeZone(REQUEST_TIME_ZONE_OFFSET_IN_HOURS));
    }

    @Test
    public void reset_link_requested_from_a_time_zone_behind_the_server_is_valid_for_the_full_period_by_the_server_clock() {
        assertResetLinkIsValidForTheFullPeriod(requestTimeZone(-REQUEST_TIME_ZONE_OFFSET_IN_HOURS));
    }

    @Test
    public void reset_link_is_sent_again_to_a_request_from_a_time_zone_ahead_of_the_server_while_it_remains_valid_long_enough_by_the_server_clock() {
        // The link is requested from the server time zone, and expires at 10:15, 15 minutes after it is sent.
        final String resetUuid = reset("USER1").orElseThrow().secret().getResetUuid();

        // At 10:09:59 by the server clock, the link remains valid for more than 5 minutes (MIN_REUSED_LINK_VALIDITY_IN_MINUTES).
        constants.setRequestTimeZone(requestTimeZone(REQUEST_TIME_ZONE_OFFSET_IN_HOURS));
        constants.setNow(dateTime("2026-10-21 10:09:59"));
        assertRequestTimeZoneShiftsNow();
        final PasswordResetLink link = reset("USER1").orElseThrow();
        assertEquals(resetUuid, link.secret().getResetUuid());
        assertEquals(MIN_REUSED_LINK_VALIDITY_IN_MINUTES, link.validityInMinutes());
    }

    /// Requests a reset link from `requestTimeZone`, and asserts that it is valid for [UserSecretCo#RESET_UUID_EXPIRATION_IN_MINUTES] by the server clock, and expires after that.
    ///
    private void assertResetLinkIsValidForTheFullPeriod(final DateTimeZone requestTimeZone) {
        final DateTime serverTimeOfRequest = constants.serverNow();
        constants.setRequestTimeZone(requestTimeZone);
        assertRequestTimeZoneShiftsNow();

        final PasswordResetLink link = reset("USER1").orElseThrow();
        assertEquals(RESET_UUID_EXPIRATION_IN_MINUTES, link.validityInMinutes());

        final String resetUuid = link.secret().getResetUuid();
        constants.setNow(serverTimeOfRequest.plusMinutes(RESET_UUID_EXPIRATION_IN_MINUTES).minusSeconds(1));
        assertTrue(isResetUuidValid(co$User(), resetUuid, dates));
        constants.setNow(serverTimeOfRequest.plusMinutes(RESET_UUID_EXPIRATION_IN_MINUTES));
        assertFalse(isResetUuidValid(co$User(), resetUuid, dates));
    }

    /// Asserts that the modelled request time zone shifts `now()` from `serverNow()`, which these tests rely on to tell them apart.
    ///
    private void assertRequestTimeZoneShiftsNow() {
        assertNotEquals("Invalid initial conditions: the request time zone should shift 'now()'.", constants.serverNow().getMillis(), constants.now().getMillis());
    }

    /// Requests a password reset at the current time of [#constants].
    /// A new cooldown for each request keeps the cooldown out of these tests.
    ///
    private Optional<PasswordResetLink> reset(final String usernameOrEmail) {
        return findOrAssignPasswordResetUuid(co$User(), new PasswordResetCooldown(), usernameOrEmail, constants);
    }

    /// A time zone with a fixed offset of `offsetInHours` from the server time zone at the modelled time.
    ///
    private DateTimeZone requestTimeZone(final int offsetInHours) {
        return DateTimeZone.forOffsetMillis(getDefault().getOffset(constants.serverNow()) + (int) HOURS.toMillis(offsetInHours));
    }

    private IUser co$User() {
        return co$(User.class);
    }

    @Override
    public boolean saveDataPopulationScriptToFile() {
        return false;
    }

    @Override
    public boolean useSavedDataPopulationScript() {
        return false;
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        co$User().save(new_(User.class, "USER1").setBase(true).setEmail("USER1@company.com").setActive(true));

        // Saving a new user assigns a reset UUID for the initial password, which this user is assumed to have used.
        final UserSecretCo coUserSecret = co$(UserSecret.class);
        coUserSecret.save(coUserSecret.findByUsername("USER1").orElseThrow().setResetUuid(null));
    }

}
