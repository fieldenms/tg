package ua.com.fielden.platform.utils;

import static org.joda.time.DateTimeZone.getDefault;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.inject.ImplementedBy;

/**
 * Contract for time-zone-aware date handling.
 * 
 * @author TG Team
 *
 */
@ImplementedBy(DefaultDates.class)
public interface IDates {
    
    /**
     * Returns a value indicating the current date/time in user's time-zone.
     * 
     * @return
     */
    default DateTime now() {
        return new DateTime();
    }
    
    /**
     * Returns time-zone context for current user.
     * 
     * @return
     */
    default DateTimeZone timeZone() {
        return getDefault();
    }
    
    /**
     * Creates zoned representation for <code>date</code> moment in user's time-zone.
     * 
     * @param date
     * @return
     */
    default DateTime zoned(final Date date) {
        return new DateTime(date);
    }
    
    /**
     * Converts moment in time to string in user's time-zone.
     * 
     * @param date
     * @return
     */
    default String toString(final Date date) {
        return EntityUtils.toString(date);
    }
    
    /**
     * Converts zoned representation of moment in time to string.
     * 
     * @param dateTime
     * @return
     */
    default String toString(final DateTime dateTime) {
        return EntityUtils.toString(dateTime);
    }
    
}