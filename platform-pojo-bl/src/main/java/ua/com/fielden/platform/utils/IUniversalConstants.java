package ua.com.fielden.platform.utils;

import java.util.Locale;

import org.joda.time.DateTime;

import com.google.inject.ImplementedBy;

/**
 * Contains a set of methods providing values of the universal constants such as <b>now</b>. This abstraction is required in order to flexibly support both unit testing and
 * production deployments.
 * 
 * @author TG Team
 * 
 */
@ImplementedBy(DefaultUniversalConstants.class)
public interface IUniversalConstants {
    
    /// Returns the current date/time in the user's time zone during a web request, as [IDates#now()].
    ///
    /// For the current date/time in the server's time zone, use [#serverNow()].
    ///
    DateTime now();

    /// Returns the current date/time in the server's time zone, regardless of any request time zone, as [IDates#serverNow()].
    ///
    /// Outside of web requests, it is the same moment as [#now()].
    ///
    default DateTime serverNow() {
        return new DateTime();
    }

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
    
}