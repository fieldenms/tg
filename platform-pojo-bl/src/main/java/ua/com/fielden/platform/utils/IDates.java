package ua.com.fielden.platform.utils;

import static org.joda.time.DateTimeZone.getDefault;

import java.util.Date;
import java.util.Optional;

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
     * Returns the day of the month, as identified by {@link #finYearStartMonth()},  when the financial year starts (in many countries it is the first day of the month, but sometimes it might be a different day, like the 6th of April).
     *
     * @return
     */
    int finYearStartDay();

    /**
     * Returns the month number (1 - January, 12 - December) when the financial year starts.
     *
     * @return
     */
    int finYearStartMonth();

    /**
     * Returns the number of the day that is a start of the week (1 - Monday, ..., 7 - Sunday)
     *
     * @return
     */
    int startOfWeek();

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
     * Returns a time-zone associated with an external request. An empty value is returned if no such information was provided.
     *
     * @return
     */
    Optional<DateTimeZone> requestTimeZone();

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