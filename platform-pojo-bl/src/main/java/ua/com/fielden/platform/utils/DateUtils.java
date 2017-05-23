package ua.com.fielden.platform.utils;

import java.util.Date;

import org.joda.time.LocalTime;

/**
 * Utility functions for working with date and time.
 * 
 * @author TG Team
 *
 */
public class DateUtils {
    private DateUtils() {}
    
    /**
     * Creates a new date as today's date with the specified hour and minute, 0 seconds
     * 
     * @param hourOfDay
     * @param minuteOfHour
     * @return
     */
    public static Date time(final int hourOfDay, final int minuteOfHour) {
        final LocalTime time = new LocalTime(hourOfDay, minuteOfHour, 0);
        return time.toDateTimeToday().toDate();
    }

}
