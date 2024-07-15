package ua.com.fielden.platform.utils;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.logging.log4j.LogManager.getLogger;
import static org.joda.time.DateTimeZone.forID;
import static org.joda.time.DateTimeZone.getDefault;
import static org.joda.time.format.DateTimeFormat.forPattern;
import static ua.com.fielden.platform.utils.EntityUtils.dateWithoutTimeFormat;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Date;
import java.util.Optional;

import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import ua.com.fielden.platform.basic.config.exceptions.ApplicationConfigurationException;

/**
 * Default implementation of {@link IDates}, which uses a thread-local state for managing time-zone values specified in client requests.
 * Request time-zones are used when converting timeline moments to time-zone specific ones.
 *
 * @author TG Team
 *
 */
@Singleton
public class DefaultDates implements IDates {
    private static final String SERVER_TIME_ZONE_APPLIED = "Server time-zone will be used.";
    private final Logger logger = getLogger(getClass());
    private final boolean independentTimeZone;
    private final Integer weekStart;
    private final Integer finYearStartDay;
    private final Integer finYearStartMonth;
    private final ThreadLocal<DateTimeZone> threadLocalRequestTimeZone = new ThreadLocal<>();

    @Inject
    public DefaultDates(
            final @Named("independent.time.zone") boolean independentTimeZone,
            final @Named("dates.weekStart") Integer weekStart,
            final @Named("dates.finYearStartDay") Integer finYearStartDay,
            final @Named("dates.finYearStartMonth") Integer finYearStartMonth) {
        // Let's validated weekStart, finYearStartDay and finYearStartMonth
        if (weekStart < 1 || weekStart > 7) {
            throw new ApplicationConfigurationException(format("Value [%s] is not acceptable for [dates.weekStart]. Expecting a number from 1 (Mon) to 7 (Sun).", weekStart));
        }
        if (finYearStartDay < 1 || finYearStartDay > 31) {
            throw new ApplicationConfigurationException(format("Value [%s] is not acceptable for [dates.finYearStartDay]. Expecting a number from 1 to 31.", finYearStartDay));
        }
        if (finYearStartMonth < 1 || finYearStartMonth > 12) {
            throw new ApplicationConfigurationException(format("Value [%s] is not acceptable for [dates.finYearStartMonth]. Expecting a number from 1 (Jan) to 12 (Dec).", finYearStartMonth));
        }
        // Day 31 has a special meaning -- this is the last day of any month.
        // This aspect is handled in DateUtilities.startOfDateRangeThatIncludes.
        // Days 29 and 30 for month 2 are not acceptable -- day 31 should be used in case of the last day of Feb needs to be specified.
        // All other values are acceptable. So, even day 31 for Apr is acceptable, because it simply means the last day of the month.
        if (finYearStartMonth == 2 && (finYearStartDay == 29 || finYearStartDay == 30)) {
            throw new ApplicationConfigurationException(format("Value [%s] is not acceptable for [dates.finYearStartDay] with [dates.finYearStartMonth] = %s. Specify 31 if the last day of the month is required.", finYearStartDay, finYearStartMonth));
        } else {
            this.finYearStartDay = finYearStartDay;
        }

        this.independentTimeZone = independentTimeZone;
        this.weekStart = weekStart;
        this.finYearStartMonth = finYearStartMonth;
    }

    @Override
    public DateTimeZone timeZone() {
        return independentTimeZone ? getDefault() : requestTimeZone().orElseGet(DateTimeZone::getDefault);
    }

    @Override
    public int startOfWeek() {
        return weekStart;
    }

    @Override
    public int finYearStartDay() {
        return finYearStartDay;
    }

    @Override
    public int finYearStartMonth() {
        return finYearStartMonth;
    }

    /**
     * Sets time-zone associated with an external request defined by {@code tzDatabaseTimeZoneId}, if any.
     * <p>
     * WARNING: every server resource should invoke one of {@code setRequestTimeZone} methods to redefine thread-local request time-zone (potentially empty);
     *          this is because threads can be reused for different resources and, if not redefined, previous request time-zone will be taken, potentially from other user and other time-zone
     *
     * @param tzDatabaseTimeZoneId -- the name of time-zone as it is defined in 'tz database' (also known as 'tzdata', 'zoneinfo database' or 'IANA time zone database');
     *                                if multiple IDs are provided and separated by comma, only first one will be used;
     *                                if empty then no time-zone will be associated with the request
     */
    public void setRequestTimeZone(final String tzDatabaseTimeZoneId) {
        if (!isEmpty(tzDatabaseTimeZoneId)) {
            // in case where multiple 'Time-Zone' headers have been returned, just use the first one; this is for additional safety if official 'Time-Zone' header will appear in future
            final String timeZoneId = tzDatabaseTimeZoneId.contains(",") ? tzDatabaseTimeZoneId.trim().split(",")[0] : tzDatabaseTimeZoneId.trim(); // ',' is not a special character in reg expressions, no need to escape it
            try {
                setRequestTimeZone(of(forID(timeZoneId))); // forID never returns null
            } catch (final IllegalArgumentException ex) {
                logger.error(format("Unknown tz database time-zone [%s]. %s", timeZoneId, SERVER_TIME_ZONE_APPLIED), ex);
                setRequestTimeZone(empty());
            }
        } else {
            // logger.debug(format("Empty client time-zone string is returned from client application. %s", SERVER_TIME_ZONE_APPLIED));
            setRequestTimeZone(empty());
        }
    }

    /**
     * Sets time-zone associated with an external request, if any.
     * <p>
     * WARNING: every server resource should invoke one of {@code setRequestTimeZone} methods to redefine thread-local request time-zone (potentially empty);
     *          this is because threads can be reused for different resources and, if not redefined, previous request time-zone will be taken, potentially from other user and other time-zone
     *
     * @param requestTimeZoneOpt -- optional time-zone as in 'tz database' (also known as 'tzdata', 'zoneinfo database' or 'IANA time zone database');
     *                              if empty then no time-zone will be associated with the request
     */
    public void setRequestTimeZone(final Optional<DateTimeZone> requestTimeZoneOpt) {
        threadLocalRequestTimeZone.set(requestTimeZoneOpt.orElse(null));
    }

    @Override
    public DateTime now() {
        return requestTimeZone().map(tz -> {
            final DateTime nowInClientTimeZone = new DateTime(tz);
            return independentTimeZone ? nowInClientTimeZone.withZoneRetainFields(getDefault()) : nowInClientTimeZone;
        })
        .orElseGet(() -> new DateTime()); // now in server time-zone; used as a fallback where no time-zone was used.
    }

    @Override
    public DateTime zoned(final Date date) {
        return new DateTime(date, timeZone());
    }

    @Override
    public String toString(final DateTime dateTime) {
        return forPattern(dateWithoutTimeFormat + " hh:mm a").print(dateTime);
    }

    @Override
    public String toString(final Date date) {
        return toString(zoned(date));
    }

    @Override
    public Optional<DateTimeZone> requestTimeZone() {
        return ofNullable(threadLocalRequestTimeZone.get());
    }

}