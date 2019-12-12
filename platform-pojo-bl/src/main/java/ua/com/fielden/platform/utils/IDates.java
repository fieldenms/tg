package ua.com.fielden.platform.utils;

import static org.joda.time.DateTimeZone.getDefault;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * Contract for time-zone-aware date handling.
 * 
 * @author TG Team
 *
 */
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
    
    default DateTime dt(final Date date) {
        return new DateTime(date);
    }
    
    default String toString(final Date date) {
        return EntityUtils.toString(date);
    }
    
    default String toString(final DateTime dateTime) {
        return EntityUtils.toString(dateTime);
    }
    
}