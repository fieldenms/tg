package ua.com.fielden.platform.utils;

import com.google.inject.ImplementedBy;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

import static org.joda.time.DateTimeZone.getDefault;

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


    /// Determines the financial year (FY) for the given `date`.
    ///
    /// If the financial year spans two calendar years, the ending year is returned.
    ///
    /// For example, for an FY from `01-Jul-2025` to `30-Jun-2026`,
    /// the value `2026` would be returned for the date `03-Nov-2025`.
    ///
    default int finYearForDate(LocalDate date) {
        return DateUtils.finYearForDate(finYearStartDay(), finYearStartMonth(), date);
    }

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

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    //:::::::::::::: Date and Time formatting ::::::::::::::::::::::::::
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    String DEFAULT_DATE_FORMAT = "dd/MM/yyyy";
    String DEFAULT_TIME_FORMAT = "HH:mm";
    String DEFAULT_TIME_FORMAT_WITH_MILLIS = "HH:mm:ss.SSS";
    String DEFAULT_DATE_FORMAT_WEB = "DD/MM/YYYY";
    String DEFAULT_TIME_FORMAT_WEB = "HH:mm";
    String DEFAULT_TIME_FORMAT_WEB_WITH_MILLIS = "HH:mm:ss.SSS";

    default String dateFormat() {
        return DEFAULT_DATE_FORMAT;
    }

    default String timeFormat() {
        return DEFAULT_TIME_FORMAT;
    }

    default String timeFormatWithMillis() {
        return DEFAULT_TIME_FORMAT_WITH_MILLIS;
    }

    default String dateFormatWeb() {
        return DEFAULT_DATE_FORMAT_WEB;
    }

    default String timeFormatWeb() {
        return DEFAULT_TIME_FORMAT_WEB;
    }

    default String timeFormatWebWithMillis() {
        return DEFAULT_TIME_FORMAT_WEB_WITH_MILLIS;
    }

    /// Converts `date` to string in the default time zone.
    ///
    /// Uses [#toStringAsTimeOnly(Date)] to format the time component.
    ///
    /// Empty string is return if `date` is `null`.
    ///
    default String toString(final Date date) {
        if (date == null) {
            return "";
        }
        else {
            return toStringAsDateOnly(date) + " " + toStringAsTimeOnly(date);
        }
    }

    /// Converts `dateTime` to string in the default time zone.
    ///
    /// See [#toString(Date)] for more details.
    ///
    default String toString(final DateTime dateTime) {
        return dateTime == null ? "" : toString(dateTime.toDate());
    }

    /// Converts `date` to a string disregarding the time component.
    ///
    default String toStringAsDateOnly(final Date date) {
        return date == null ? "" : new SimpleDateFormat(dateFormat()).format(date);
    }

    /// Converts `date` to a string disregarding the date component.
    ///
    /// If the time component includes seconds or millis the [#timeFormatWithMillis()] is used for formatting.
    /// Otherwise, [#timeFormat()] is used instead.
    ///
    /// The default time zone is assumed here.
    ///
    default String toStringAsTimeOnly(final Date date) {
        if (date == null) {
            return  "";
        }
        else {
            final Instant instant = date.toInstant();
            final ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());

            boolean hasMillis = (instant.toEpochMilli() % 1000) != 0;
            boolean hasSeconds = zdt.getSecond() != 0;
            return hasSeconds || hasMillis
                   ? new SimpleDateFormat(timeFormatWithMillis()).format(date)
                   : new SimpleDateFormat(timeFormat()).format(date);
        }
    }

}