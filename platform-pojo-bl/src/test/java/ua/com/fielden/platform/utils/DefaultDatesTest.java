package ua.com.fielden.platform.utils;

import static java.lang.Math.abs;
import static org.joda.time.DateTimeZone.forID;
import static org.joda.time.DateTimeZone.getDefault;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.joda.time.DateTime;
import org.junit.Test;

public class DefaultDatesTest {
    private final Date date = new Date(0L);
    
    // Note: choose unlikely time-zones for testing: Pacific/Kiritimati (UTC+14, far east) and Pacific/Pago_Pago (UTC-11, far west).
    //   This is with intention for DateTimeZone.getDefault() not being equal to that testing time-zone.
    
    @Test
    public void request_time_zone_gets_properly_set_in_current_thread() {
        final DefaultDates dates = new DefaultDates(true);
        
        final String testingTimeZoneId = "Pacific/Kiritimati";
        dates.setRequestTimeZone(testingTimeZoneId); 
        
        assertEquals(forID(testingTimeZoneId), dates.requestTimeZone());
    }
    
    @Test
    public void request_time_zone_gets_properly_set_in_current_and_another_thread_and_they_differ() throws InterruptedException, ExecutionException {
        final DefaultDates dates = new DefaultDates(true);
        
        final String testingTimeZoneId = "Pacific/Kiritimati";
        dates.setRequestTimeZone(testingTimeZoneId); 
        
        assertEquals(forID(testingTimeZoneId), dates.requestTimeZone());
        
        final boolean result = CompletableFuture.supplyAsync(() -> {
            // start another thread with different request time-zone setting
            final String anotherTestingTimeZoneId = "Pacific/Pago_Pago";
            dates.setRequestTimeZone(anotherTestingTimeZoneId);
            
            return forID(anotherTestingTimeZoneId).equals(dates.requestTimeZone());
        }).get();
        if (!result) {
            fail("Time zone on different thread should be different.");
        }
        assertEquals(forID(testingTimeZoneId), dates.requestTimeZone()); // the time-zone on this thread is still the same
    }
    
    @Test
    public void null_request_time_zone_setting_leaves_it_empty() {
        final DefaultDates dates = new DefaultDates(true);
        
        dates.setRequestTimeZone(null);
        
        assertNull(dates.requestTimeZone());
    }
    
    @Test
    public void empty_request_time_zone_setting_leaves_it_empty() {
        final DefaultDates dates = new DefaultDates(true);
        
        dates.setRequestTimeZone("");
        
        assertNull(dates.requestTimeZone());
    }
    
    @Test
    public void non_trimmed_request_time_zone_gets_properly_set() {
        final DefaultDates dates = new DefaultDates(true);
        
        dates.setRequestTimeZone("  Pacific/Kiritimati \n");
        
        assertEquals(forID("Pacific/Kiritimati"), dates.requestTimeZone());
    }
    
    @Test
    public void the_first_request_time_zone_is_used_out_of_several_ones() {
        final DefaultDates dates = new DefaultDates(true);
        
        dates.setRequestTimeZone("  Pacific/Kiritimati,Pacific/Pago_Pago  ");
        
        assertEquals(forID("Pacific/Kiritimati"), dates.requestTimeZone());
    }
    
    @Test
    public void unknown_request_time_zone_is_disregarded() {
        final DefaultDates dates = new DefaultDates(true);
        
        dates.setRequestTimeZone("Pacific/BlaBlaBla");
        
        assertNull(dates.requestTimeZone());
    }
    
    // timeZone() method:
    
    @Test
    public void time_zone_equals_to_default_in_independent_mode_with_empty_request_time_zone() {
        final DefaultDates dates = new DefaultDates(true);
        assertNull(dates.requestTimeZone());
        
        assertEquals(getDefault(), dates.timeZone());
    }
    
    @Test
    public void time_zone_equals_to_default_in_independent_mode_even_with_non_empty_request_time_zone() {
        final DefaultDates dates = new DefaultDates(true);
        final String testingTimeZoneId = "Pacific/Kiritimati";
        dates.setRequestTimeZone(testingTimeZoneId); 
        assertEquals(forID(testingTimeZoneId), dates.requestTimeZone());
        
        assertEquals(getDefault(), dates.timeZone());
    }
    
    @Test
    public void time_zone_equals_to_default_in_dependent_mode_with_empty_request_time_zone() {
        final DefaultDates dates = new DefaultDates(false);
        assertNull(dates.requestTimeZone());
        
        assertEquals(getDefault(), dates.timeZone());
    }
    
    @Test
    public void time_zone_equals_to_non_empty_request_time_zone_in_dependent_mode() {
        final DefaultDates dates = new DefaultDates(false);
        final String testingTimeZoneId = "Pacific/Kiritimati";
        dates.setRequestTimeZone(testingTimeZoneId); 
        assertEquals(forID(testingTimeZoneId), dates.requestTimeZone());
        
        assertEquals(forID(testingTimeZoneId), dates.timeZone());
    }
    
    // zoned() method:
    
    @Test
    public void zoned_moment_has_default_time_zone_in_independent_mode_with_empty_request_time_zone() {
        final DefaultDates dates = new DefaultDates(true);
        assertNull(dates.requestTimeZone());
        
        assertEquals(new DateTime(date, getDefault()), dates.zoned(date));
    }
    
    @Test
    public void zoned_moment_has_default_time_zone_in_independent_mode_even_with_non_empty_request_time_zone() {
        final DefaultDates dates = new DefaultDates(true);
        final String testingTimeZoneId = "Pacific/Kiritimati";
        dates.setRequestTimeZone(testingTimeZoneId); 
        assertEquals(forID(testingTimeZoneId), dates.requestTimeZone());
        
        assertEquals(new DateTime(date, getDefault()), dates.zoned(date));
    }
    
    @Test
    public void zoned_moment_has_default_time_zone_in_dependent_mode_with_empty_request_time_zone() {
        final DefaultDates dates = new DefaultDates(false);
        assertNull(dates.requestTimeZone());
        
        assertEquals(new DateTime(date, getDefault()), dates.zoned(date));
    }
    
    @Test
    public void zoned_moment_has_non_empty_request_time_zone_in_dependent_mode() {
        final DefaultDates dates = new DefaultDates(false);
        final String testingTimeZoneId = "Pacific/Kiritimati";
        dates.setRequestTimeZone(testingTimeZoneId); 
        assertEquals(forID(testingTimeZoneId), dates.requestTimeZone());
        
        assertEquals(new DateTime(date, forID(testingTimeZoneId)), dates.zoned(date));
    }
    
    // toString() method:
    
    @Test
    public void date_prints_in_default_time_zone_in_independent_mode_with_empty_request_time_zone() {
        final DefaultDates dates = new DefaultDates(true);
        assertNull(dates.requestTimeZone());
        
        assertEquals(dates.toString(new DateTime(date, getDefault())), dates.toString(date));
    }
    
    @Test
    public void date_prints_in_default_time_zone_in_independent_mode_even_with_non_empty_request_time_zone() {
        final DefaultDates dates = new DefaultDates(true);
        final String testingTimeZoneId = "Pacific/Kiritimati";
        dates.setRequestTimeZone(testingTimeZoneId); 
        assertEquals(forID(testingTimeZoneId), dates.requestTimeZone());
        
        assertEquals(dates.toString(new DateTime(date, getDefault())), dates.toString(date));
    }
    
    @Test
    public void date_prints_in_default_time_zone_in_dependent_mode_with_empty_request_time_zone() {
        final DefaultDates dates = new DefaultDates(false);
        assertNull(dates.requestTimeZone());
        
        assertEquals(dates.toString(new DateTime(date, getDefault())), dates.toString(date));
    }
    
    @Test
    public void date_prints_in_non_empty_request_time_zone_in_dependent_mode() {
        final DefaultDates dates = new DefaultDates(false);
        final String testingTimeZoneId = "Pacific/Kiritimati";
        dates.setRequestTimeZone(testingTimeZoneId); 
        assertEquals(forID(testingTimeZoneId), dates.requestTimeZone());
        
        assertEquals(dates.toString(new DateTime(date, forID(testingTimeZoneId))), dates.toString(date));
    }
    
    // now() method:
    
    private final Long MILLIS_DIFF_THRESHOULD = 10L; // millis are close enough if they differ in less than MILLIS_DIFF_THRESHOULD millis; 2L is sufficient in most cases, take 10L to be sure
    
    @Test
    public void now_moment_is_in_default_time_zone_in_independent_mode_with_empty_request_time_zone() {
        final DefaultDates dates = new DefaultDates(true);
        assertNull(dates.requestTimeZone());
        
        assertTrue(abs(new DateTime(getDefault()).getMillis() - dates.now().getMillis()) < MILLIS_DIFF_THRESHOULD);
    }
    
    @Test
    public void now_moment_is_in_default_time_zone_but_with_values_from_real_request_time_zone_in_independent_mode_if_request_time_zone_is_not_empty() {
        final DefaultDates dates = new DefaultDates(true);
        final String testingTimeZoneId = "Pacific/Kiritimati";
        dates.setRequestTimeZone(testingTimeZoneId); 
        assertEquals(forID(testingTimeZoneId), dates.requestTimeZone());
        
        assertTrue(abs(new DateTime(dates.requestTimeZone()).withZoneRetainFields(getDefault()).getMillis() - dates.now().getMillis()) < MILLIS_DIFF_THRESHOULD);
    }
    
    @Test
    public void now_moment_is_in_default_time_zone_in_dependent_mode_with_empty_request_time_zone() {
        final DefaultDates dates = new DefaultDates(false);
        assertNull(dates.requestTimeZone());
        
        assertTrue(abs(new DateTime(getDefault()).getMillis() - dates.now().getMillis()) < MILLIS_DIFF_THRESHOULD);
    }
    
    @Test
    public void now_is_in_non_empty_request_time_zone_in_dependent_mode() {
        final DefaultDates dates = new DefaultDates(false);
        final String testingTimeZoneId = "Pacific/Kiritimati";
        dates.setRequestTimeZone(testingTimeZoneId); 
        assertEquals(forID(testingTimeZoneId), dates.requestTimeZone());
        
        assertTrue(abs(new DateTime(dates.requestTimeZone()).getMillis() - dates.now().getMillis()) < MILLIS_DIFF_THRESHOULD);
    }
    
}