package ua.com.fielden.platform.test.ioc;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import static org.joda.time.DateTimeZone.UTC;
import static org.joda.time.DateTimeZone.forID;
import static org.joda.time.DateTimeZone.getDefault;
import static org.junit.Assert.*;

public class DatesForTestingTest {

    // Note: choose an unlikely time-zone for testing: Pacific/Kiritimati (UTC+14, far east, no daylight saving).
    //   This is with intention for DateTimeZone.getDefault() not being equal to that testing time-zone.
    private static final DateTimeZone REQUEST_TIME_ZONE = forID("Pacific/Kiritimati");

    // The modelled time is in UTC, so that its wall-clock time in the request time zone does not depend on the default time zone.
    private final DateTime modelledNow = new DateTime(2026, 10, 21, 9, 30, UTC);
    private final LocalDateTime modelledNowInRequestTimeZone = new LocalDateTime(2026, 10, 21, 23, 30);

    @Test
    public void now_and_serverNow_return_the_modelled_time_without_a_request_time_zone() {
        final DatesForTesting dates = new DatesForTesting();
        dates.setNow(modelledNow);

        assertFalse(dates.requestTimeZone().isPresent());
        assertEquals(modelledNow, dates.now());
        assertEquals(modelledNow, dates.serverNow());
    }

    @Test
    public void serverNow_returns_the_modelled_time_and_now_returns_it_relabelled_from_a_modelled_request_time_zone() {
        final DatesForTesting dates = new DatesForTesting();
        dates.setNow(modelledNow);
        dates.setRequestTimeZone(REQUEST_TIME_ZONE);

        assertEquals(REQUEST_TIME_ZONE, dates.requestTimeZone().orElseThrow());
        assertEquals(modelledNow, dates.serverNow());

        final DateTime now = dates.now();
        assertEquals(getDefault(), now.getZone());
        assertEquals(modelledNowInRequestTimeZone, now.toLocalDateTime());

        // Independent time-zone mode is modelled: the time zone for converting moments stays the server's time zone.
        assertEquals(getDefault(), dates.timeZone());
        assertEquals(getDefault(), dates.zoned(modelledNow.toDate()).getZone());
    }

    @Test
    public void serverNow_uses_a_time_supplier_regardless_of_a_modelled_request_time_zone() {
        final DatesForTesting dates = new DatesForTesting();
        dates.setTimeSupplier(() -> modelledNow).setRequestTimeZone(REQUEST_TIME_ZONE);

        assertEquals(modelledNow, dates.serverNow());
        assertEquals(modelledNowInRequestTimeZone, dates.now().toLocalDateTime());
    }

    @Test
    public void null_request_time_zone_is_rejected() {
        final DatesForTesting dates = new DatesForTesting();
        assertThrows(NullPointerException.class, () -> dates.setRequestTimeZone(null));
        assertFalse(dates.requestTimeZone().isPresent());
    }

    @Test
    public void setting_a_null_time_supplier_removes_a_time_supplier() {
        final DatesForTesting dates = new DatesForTesting();
        dates.setNow(modelledNow);
        dates.setTimeSupplier(() -> modelledNow.plusDays(1));
        assertEquals(modelledNow.plusDays(1), dates.now());

        dates.setTimeSupplier(null);
        assertEquals(modelledNow, dates.now());
    }

    @Test
    public void removing_a_modelled_request_time_zone_makes_now_and_serverNow_equal_again() {
        final DatesForTesting dates = new DatesForTesting();
        dates.setNow(modelledNow);
        dates.setRequestTimeZone(REQUEST_TIME_ZONE).removeRequestTimeZone();

        assertFalse(dates.requestTimeZone().isPresent());
        assertEquals(modelledNow, dates.now());
        assertEquals(modelledNow, dates.serverNow());
    }

    @Test
    public void UniversalConstantsForTesting_delegates_now_and_serverNow_to_DatesForTesting() {
        final DatesForTesting dates = new DatesForTesting();
        final UniversalConstantsForTesting constants = new UniversalConstantsForTesting("app", "smtp", "from@unit-test.software", dates);
        constants.setNow(modelledNow);
        constants.setRequestTimeZone(REQUEST_TIME_ZONE);

        assertEquals(modelledNow, constants.serverNow());
        assertEquals(dates.now(), constants.now());
        assertNotEquals(constants.serverNow(), constants.now());

        constants.removeRequestTimeZone();
        assertEquals(modelledNow, constants.now());
        assertEquals(modelledNow, constants.serverNow());
    }

}
