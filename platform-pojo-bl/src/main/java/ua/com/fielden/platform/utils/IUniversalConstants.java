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
     * Return a value indicating the current date/time.
     * 
     * @return
     */
    DateTime now();

    /**
     * Return a value indicating the current locale.
     * 
     * @return
     */
    Locale locale();
}
