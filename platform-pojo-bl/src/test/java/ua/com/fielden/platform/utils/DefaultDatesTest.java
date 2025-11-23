package ua.com.fielden.platform.utils;

import org.joda.time.DateTime;
import org.junit.Test;
import ua.com.fielden.platform.basic.config.exceptions.ApplicationConfigurationException;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.lang.Math.abs;
import static org.joda.time.DateTimeZone.forID;
import static org.joda.time.DateTimeZone.getDefault;
import static org.junit.Assert.*;

public class DefaultDatesTest {
    private final Date date = new Date(0L);

    // Note: choose unlikely time-zones for testing: Pacific/Kiritimati (UTC+14, far east) and Pacific/Pago_Pago (UTC-11, far west).
    //   This is with intention for DateTimeZone.getDefault() not being equal to that testing time-zone.

    @Test
    public void request_time_zone_gets_properly_set_in_current_thread() {
        final DefaultDates dates = getDefaultDates(true);

        final String testingTimeZoneId = "Pacific/Kiritimati";
        dates.setRequestTimeZone(testingTimeZoneId);

        assertTrue(dates.requestTimeZone().isPresent());
        assertEquals(forID(testingTimeZoneId), dates.requestTimeZone().get());
    }

    @Test
    public void request_time_zone_gets_properly_set_in_current_thread_and_can_be_redefined() {
        final DefaultDates dates = getDefaultDates(true);

        final String testingTimeZoneId = "Pacific/Kiritimati";
        dates.setRequestTimeZone(testingTimeZoneId);

        assertTrue(dates.requestTimeZone().isPresent());
        assertEquals(forID(testingTimeZoneId), dates.requestTimeZone().get());

        final String anotherTestingTimeZoneId = "Pacific/Pago_Pago";
        dates.setRequestTimeZone(anotherTestingTimeZoneId);

        assertTrue(dates.requestTimeZone().isPresent());
        assertEquals(forID(anotherTestingTimeZoneId), dates.requestTimeZone().get());
    }

    @Test
    public void request_time_zone_gets_properly_set_in_current_and_another_thread_and_they_differ() throws InterruptedException, ExecutionException {
        final DefaultDates dates = getDefaultDates(true);

        final String testingTimeZoneId = "Pacific/Kiritimati";
        dates.setRequestTimeZone(testingTimeZoneId);

        assertTrue(dates.requestTimeZone().isPresent());
        assertEquals(forID(testingTimeZoneId), dates.requestTimeZone().get());

        final boolean result = CompletableFuture.supplyAsync(() -> {
            // start another thread with different request time-zone setting
            final String anotherTestingTimeZoneId = "Pacific/Pago_Pago";
            dates.setRequestTimeZone(anotherTestingTimeZoneId);

            return forID(anotherTestingTimeZoneId).equals(dates.requestTimeZone().orElse(null));
        }).get();

        if (!result) {
            fail("Time zone on different thread should be different.");
        }
        assertEquals(forID(testingTimeZoneId), dates.requestTimeZone().get()); // the time-zone on this thread is still the same
    }

    @Test
    public void null_request_time_zone_setting_leaves_it_empty() {
        final DefaultDates dates = getDefaultDates(true);

        dates.setRequestTimeZone((String) null);

        assertFalse(dates.requestTimeZone().isPresent());
    }

    @Test
    public void null_request_time_zone_setting_redefines_previous_by_making_it_empty() {
        final DefaultDates dates = getDefaultDates(true);

        final String testingTimeZoneId = "Pacific/Kiritimati";
        dates.setRequestTimeZone(testingTimeZoneId);

        final String anotherTestingTimeZoneId = null;
        dates.setRequestTimeZone(anotherTestingTimeZoneId);

        assertFalse(dates.requestTimeZone().isPresent());
    }

    @Test
    public void empty_request_time_zone_setting_leaves_it_empty() {
        final DefaultDates dates = getDefaultDates(true);

        dates.setRequestTimeZone("");

        assertFalse(dates.requestTimeZone().isPresent());
    }

    @Test
    public void empty_request_time_zone_setting_redefines_previous_by_making_it_empty() {
        final DefaultDates dates = getDefaultDates(true);

        final String testingTimeZoneId = "Pacific/Kiritimati";
        dates.setRequestTimeZone(testingTimeZoneId);

        final String anotherTestingTimeZoneId = "";
        dates.setRequestTimeZone(anotherTestingTimeZoneId);

        assertFalse(dates.requestTimeZone().isPresent());
    }

    @Test
    public void non_trimmed_request_time_zone_gets_properly_set() {
        final DefaultDates dates = getDefaultDates(true);

        dates.setRequestTimeZone("  Pacific/Kiritimati \n");

        assertTrue(dates.requestTimeZone().isPresent());
        assertEquals(forID("Pacific/Kiritimati"), dates.requestTimeZone().get());
    }

    @Test
    public void the_first_request_time_zone_is_used_out_of_several_ones() {
        final DefaultDates dates = getDefaultDates(true);

        dates.setRequestTimeZone("  Pacific/Kiritimati,Pacific/Pago_Pago  ");

        assertTrue(dates.requestTimeZone().isPresent());
        assertEquals(forID("Pacific/Kiritimati"), dates.requestTimeZone().get());
    }

    @Test
    public void unknown_request_time_zone_is_disregarded() {
        final DefaultDates dates = getDefaultDates(true);

        dates.setRequestTimeZone("Pacific/BlaBlaBla");

        assertFalse(dates.requestTimeZone().isPresent());
    }

    @Test
    public void unknown_request_time_zone_setting_redefines_previous_by_making_it_empty() {
        final DefaultDates dates = getDefaultDates(true);

        final String testingTimeZoneId = "Pacific/Kiritimati";
        dates.setRequestTimeZone(testingTimeZoneId);

        final String anotherTestingTimeZoneId = "Pacific/BlaBlaBla";
        dates.setRequestTimeZone(anotherTestingTimeZoneId);

        assertFalse(dates.requestTimeZone().isPresent());
    }

    // timeZone() method:

    @Test
    public void time_zone_equals_to_default_in_independent_mode_with_empty_request_time_zone() {
        final DefaultDates dates = getDefaultDates(true);
        assertFalse(dates.requestTimeZone().isPresent());

        assertEquals(getDefault(), dates.timeZone());
    }

    @Test
    public void time_zone_equals_to_default_in_independent_mode_even_with_non_empty_request_time_zone() {
        final DefaultDates dates = getDefaultDates(true);
        final String testingTimeZoneId = "Pacific/Kiritimati";
        dates.setRequestTimeZone(testingTimeZoneId);

        assertTrue(dates.requestTimeZone().isPresent());
        assertEquals(forID(testingTimeZoneId), dates.requestTimeZone().get());

        assertEquals(getDefault(), dates.timeZone());
    }

    @Test
    public void time_zone_equals_to_default_in_dependent_mode_with_empty_request_time_zone() {
        final DefaultDates dates = getDefaultDates(false);
        assertFalse(dates.requestTimeZone().isPresent());

        assertEquals(getDefault(), dates.timeZone());
    }

    @Test
    public void time_zone_equals_to_non_empty_request_time_zone_in_dependent_mode() {
        final DefaultDates dates = getDefaultDates(false);
        final String testingTimeZoneId = "Pacific/Kiritimati";
        dates.setRequestTimeZone(testingTimeZoneId);

        assertTrue(dates.requestTimeZone().isPresent());
        assertEquals(forID(testingTimeZoneId), dates.requestTimeZone().get());

        assertEquals(forID(testingTimeZoneId), dates.timeZone());
    }

    // zoned() method:

    @Test
    public void zoned_moment_has_default_time_zone_in_independent_mode_with_empty_request_time_zone() {
        final DefaultDates dates = getDefaultDates(true);
        assertFalse(dates.requestTimeZone().isPresent());

        assertEquals(new DateTime(date, getDefault()), dates.zoned(date));
    }

    @Test
    public void zoned_moment_has_default_time_zone_in_independent_mode_even_with_non_empty_request_time_zone() {
        final DefaultDates dates = getDefaultDates(true);
        final String testingTimeZoneId = "Pacific/Kiritimati";
        dates.setRequestTimeZone(testingTimeZoneId);

        assertTrue(dates.requestTimeZone().isPresent());
        assertEquals(forID(testingTimeZoneId), dates.requestTimeZone().get());

        assertEquals(new DateTime(date, getDefault()), dates.zoned(date));
    }

    @Test
    public void zoned_moment_has_default_time_zone_in_dependent_mode_with_empty_request_time_zone() {
        final DefaultDates dates = getDefaultDates(false);
        assertFalse(dates.requestTimeZone().isPresent());

        assertEquals(new DateTime(date, getDefault()), dates.zoned(date));
    }

    @Test
    public void zoned_moment_has_non_empty_request_time_zone_in_dependent_mode() {
        final DefaultDates dates = getDefaultDates(false);
        final String testingTimeZoneId = "Pacific/Kiritimati";
        dates.setRequestTimeZone(testingTimeZoneId);

        assertTrue(dates.requestTimeZone().isPresent());
        assertEquals(forID(testingTimeZoneId), dates.requestTimeZone().get());

        assertEquals(new DateTime(date, forID(testingTimeZoneId)), dates.zoned(date));
    }

    // toString() method:

    @Test
    public void date_prints_in_default_time_zone_in_independent_mode_with_empty_request_time_zone() {
        final DefaultDates dates = getDefaultDates(true);
        assertFalse(dates.requestTimeZone().isPresent());

        assertEquals(dates.toString(new DateTime(date, getDefault())), dates.toString(date));
    }

    @Test
    public void date_prints_in_default_time_zone_in_independent_mode_even_with_non_empty_request_time_zone() {
        final DefaultDates dates = getDefaultDates(true);
        final String testingTimeZoneId = "Pacific/Kiritimati";
        dates.setRequestTimeZone(testingTimeZoneId);

        assertTrue(dates.requestTimeZone().isPresent());
        assertEquals(forID(testingTimeZoneId), dates.requestTimeZone().get());

        assertEquals(dates.toString(new DateTime(date, getDefault())), dates.toString(date));
    }

    @Test
    public void date_prints_in_default_time_zone_in_dependent_mode_with_empty_request_time_zone() {
        final DefaultDates dates = getDefaultDates(false);
        assertFalse(dates.requestTimeZone().isPresent());

        assertEquals(dates.toString(new DateTime(date, getDefault())), dates.toString(date));
    }

    @Test
    public void date_prints_in_non_empty_request_time_zone_in_dependent_mode() {
        final DefaultDates dates = getDefaultDates(false);
        final String testingTimeZoneId = "Pacific/Kiritimati";
        dates.setRequestTimeZone(testingTimeZoneId);

        assertTrue(dates.requestTimeZone().isPresent());
        assertEquals(forID(testingTimeZoneId), dates.requestTimeZone().get());

        assertEquals(dates.toString(new DateTime(date, forID(testingTimeZoneId))), dates.toString(date));
    }

    @Test
    public void date_toStringTimeOnly_excludes_date() {
        final IDates dates = getDefaultDates(false);
        final var date = new DateTime("2025-11-23T22:05").toDate();
        assertEquals("22:05", dates.toStringAsTimeOnly(date));
    }

    @Test
    public void date_toStringTimeOnly_includes_seconds_and_millis_if_either_seconds_or_millis_are_present() {
        final IDates dates = getDefaultDates(false);

        final var dateWithSeconds = new DateTime("2025-11-23T22:05:19").toDate();
        assertEquals("22:05:19.000", dates.toStringAsTimeOnly(dateWithSeconds));
        final var dateWithMillis = new DateTime("2025-11-23T22:05:00.900").toDate();
        assertEquals("22:05:00.900", dates.toStringAsTimeOnly(dateWithMillis));
    }

    @Test
    public void date_toStringDateOnly_excludes_time() {
        final IDates dates = getDefaultDates(false);
        final var date = new DateTime("2025-11-23T22:05:19").toDate();
        assertEquals("23/11/2025", dates.toStringAsDateOnly(date));
    }

    @Test
    public void date_toString_includes_both_date_and_time_where_time_contains_seconds_and_millis_if_either_seconds_or_millis_are_present() {
        final IDates dates = getDefaultDates(false);

        final var dateWithMinutes = new DateTime("2025-11-23T22:05").toDate();
        assertEquals("23/11/2025 22:05", dates.toString(dateWithMinutes));

        final var dateWithSeconds = new DateTime("2025-11-23T22:05:19").toDate();
        assertEquals("23/11/2025 22:05:19.000", dates.toString(dateWithSeconds));

        final var dateWithMillis = new DateTime("2025-11-23T22:05:00.900").toDate();
        assertEquals("23/11/2025 22:05:00.900", dates.toString(dateWithMillis));
    }


    private final Long MILLIS_DIFF_THRESHOLD = 1000L; // 1 second precision when comparing "now" values is good enough in this case

    @Test
    public void now_moment_is_in_default_time_zone_in_independent_mode_with_empty_request_time_zone() {
        final DefaultDates dates = getDefaultDates(true);
        assertFalse(dates.requestTimeZone().isPresent());

        assertTrue(abs(new DateTime(getDefault()).getMillis() - dates.now().getMillis()) < MILLIS_DIFF_THRESHOLD);
    }

    @Test
    public void now_moment_is_in_default_time_zone_but_with_values_from_real_request_time_zone_in_independent_mode_if_request_time_zone_is_not_empty() {
        final DefaultDates dates = getDefaultDates(true);
        final String testingTimeZoneId = "Pacific/Kiritimati";
        dates.setRequestTimeZone(testingTimeZoneId);

        assertTrue(dates.requestTimeZone().isPresent());
        assertEquals(forID(testingTimeZoneId), dates.requestTimeZone().get());

        assertTrue(abs(new DateTime(dates.requestTimeZone().get()).withZoneRetainFields(getDefault()).getMillis() - dates.now().getMillis()) < MILLIS_DIFF_THRESHOLD);
    }

    @Test
    public void now_moment_is_in_default_time_zone_in_dependent_mode_with_empty_request_time_zone() {
        final DefaultDates dates = getDefaultDates(false);
        assertFalse(dates.requestTimeZone().isPresent());

        assertTrue(abs(new DateTime(getDefault()).getMillis() - dates.now().getMillis()) < MILLIS_DIFF_THRESHOLD);
    }

    @Test
    public void now_is_in_non_empty_request_time_zone_in_dependent_mode() {
        final DefaultDates dates = getDefaultDates(false);
        final String testingTimeZoneId = "Pacific/Kiritimati";
        dates.setRequestTimeZone(testingTimeZoneId);
        assertTrue(dates.requestTimeZone().isPresent());
        assertEquals(forID(testingTimeZoneId), dates.requestTimeZone().get());

        assertTrue(abs(new DateTime(dates.requestTimeZone().get()).getMillis() - dates.now().getMillis()) < MILLIS_DIFF_THRESHOLD);
    }

    @Test
    public void validation_during_default_dates_instantiation_prevents_invalid_weekStart() {
        assertThrows(ApplicationConfigurationException.class, () -> new DefaultDates(false, 0, 1, 7));
        assertThrows(ApplicationConfigurationException.class, () -> new DefaultDates(false, 8, 1, 7));
    }

    @Test
    public void validation_during_default_dates_instantiation_prevents_invalid_finYearStartDay() {
        assertThrows(ApplicationConfigurationException.class, () -> new DefaultDates(false, 1, 0, 7));
        assertThrows(ApplicationConfigurationException.class, () -> new DefaultDates(false, 1, 32, 7));
    }

    @Test
    public void validation_during_default_dates_instantiation_prevents_invalid_finYearStartMonth() {
        assertThrows(ApplicationConfigurationException.class, () -> new DefaultDates(false, 1, 1, 0));
        assertThrows(ApplicationConfigurationException.class, () -> new DefaultDates(false, 1, 1, 13));
    }

    @Test
    public void validation_during_default_dates_instantiation_prevents_invalid_finYearStartDay_and_finYearStartMonth_combinations() {
        assertThrows(ApplicationConfigurationException.class, () -> new DefaultDates(false, 1, 29, 2));
        assertThrows(ApplicationConfigurationException.class, () -> new DefaultDates(false, 1, 30, 2));
    }
    
    private DefaultDates getDefaultDates(final boolean independentTimeZone) {
        return new DefaultDates(independentTimeZone, 1, 1, 7);
    }

}