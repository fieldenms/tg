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
    /**
     * Returns a value indicating the current date/time.
     * 
     * @return
     */
    DateTime now();

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
    String smptServer();
    
    /**
     * Returns a generic application <code>From</code> email address that should be used when sending emails if no more specific email address is available.
     * 
     * @return
     */
    String fromEmailAddress();
}
