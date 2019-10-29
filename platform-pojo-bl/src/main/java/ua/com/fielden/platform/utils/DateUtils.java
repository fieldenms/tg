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

    /**
     * Returns the earlier of the two dates. It considers {@code null} as a later date.
     * Value of {@code null} is returned only if both arguments are {@code null}.
     * 
     * @param date1
     * @param date2
     * @return
     */
    public static Date min(final Date date1, final Date date2) {
        if (date1 == null) {
            return date2;
        } else if (date2 == null) {
            return date1;
        } else {
            return date1.before(date2) ? date1 : date2;
        }
    }

    /**
     * Returns the later of the two dates. It considers {@code null} as an earlier date.
     * Value of {@code null} is returned only if both arguments are {@code null}.
     * 
     * @param date1
     * @param date2
     * @return
     */
    public static Date max(final Date date1, final Date date2) {
        if (date1 == null) {
            return date2;
        } else if (date2 == null) {
            return date1;
        } else {
            return date1.before(date2) ? date2 : date1;
        }
    }

}
