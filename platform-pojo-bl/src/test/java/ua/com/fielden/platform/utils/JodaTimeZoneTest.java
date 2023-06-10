package ua.com.fielden.platform.utils;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;
import org.junit.Test;

/**
 * Test case for checking time zone defaults in Joda Time library.
 * 
 * @author TG Team
 *
 */
public class JodaTimeZoneTest {

    @Test
    public void default_joda_timezone_equals_to_default_java_timezone() {
        assertEquals(org.joda.time.DateTimeZone.getDefault().toTimeZone(), java.util.TimeZone.getDefault());
    }

    @Test
    public void date_parsed_in_java_timezone_converts_to_joda_date_with_the_same_field_values() throws ParseException {
        final Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2009-03-01 11:00:55"); // uses default java timezone
        final DateTime dateTime = new DateTime(date.getTime()); // uses default joda timezone
        assertEquals(2009, dateTime.getYear());
        assertEquals(03, dateTime.getMonthOfYear());
        assertEquals(01, dateTime.getDayOfMonth());
        assertEquals(11, dateTime.getHourOfDay());
        assertEquals(00, dateTime.getMinuteOfHour());
        assertEquals(55, dateTime.getSecondOfMinute());
    }

}