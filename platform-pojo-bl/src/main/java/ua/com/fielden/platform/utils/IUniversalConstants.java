package ua.com.fielden.platform.utils;

import java.util.Date;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import static org.joda.time.DateTimeZone.getDefault;

/**
 * Contains a set of methods providing values of the universal constants such as <b>now</b>. This abstraction is required in order to flexibly support both unit testing and
 * production deployments.
 * 
 * @author TG Team
 * 
 */
public interface IUniversalConstants {
    
    /**
     * Returns a value indicating the current date/time.
     * 
     * @return
     */
    DateTime now();
    
    /**
     * The same is {@link #now()}, but with 0 time.
     */
    default DateTime today() {
        return now().withMillisOfDay(0);
    }

    /**
     * Returns a value indicating the current locale.
     * 
     * @return
     */
    Locale locale();
    
    /**
     * Returns an application name.
     * 
     * @return
     */
    String appName();
    
    /**
     * Returns an SMTP server IP address.
     * 
     * @return
     */
    String smtpServer();
    
    /**
     * Returns a generic application <code>From</code> email address that should be used when sending emails if no more specific email address is available.
     * 
     * @return
     */
    String fromEmailAddress();
    
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