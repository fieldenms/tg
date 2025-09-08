package ua.com.fielden.platform.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static ua.com.fielden.platform.entity.exceptions.InvalidArgumentException.requireNotNullArgument;

/// Utility functions for working with date and time.
///
public class DateUtils {
    private DateUtils() {}
    
    /// Creates a new date as today's date with the specified hour and minute, 0 seconds
    ///
    public static Date time(final int hourOfDay, final int minuteOfHour) {
        requireNotNullArgument(hourOfDay, "hourOfDay");
        requireNotNullArgument(minuteOfHour, "minuteOfHour");

        final LocalTime time = LocalTime.of(hourOfDay, minuteOfHour, 0, 0);
        return Date.from(time.atDate(LocalDate.now())
                         .atZone(ZoneId.systemDefault())
                         .toInstant());
    }

    /// Returns the earlier of the two dates. It considers `null` as a later date.
    /// Value of `null` is returned only if both arguments are `null`.
    ///
    public static Date min(final Date date1, final Date date2) {
        if (date1 == null) {
            return date2;
        } else if (date2 == null) {
            return date1;
        } else {
            return date1.before(date2) ? date1 : date2;
        }
    }

    /// Returns the later of the two dates. It considers `null` as an earlier date.
    /// Value of `null` is returned only if both arguments are `null`.
    ///
    public static Date max(final Date date1, final Date date2) {
        if (date1 == null) {
            return date2;
        } else if (date2 == null) {
            return date1;
        } else {
            return date1.before(date2) ? date2 : date1;
        }
    }

    /// Creates a new date with the date part from `dateWithDatePart` and the time part from `dateWithTimePart`.
    ///
    /// This method does not handle DST situations explicitly, relying on the Java Time API to do the right thing.
    /// As the result, it possible that the resultant time would either jump forward or fall back.
    ///
    public static Date mergeDateAndTime(final Date dateWithDatePart, final Date dateWithTimePart) {
        requireNotNullArgument(dateWithDatePart, "dateWithDatePart");
        requireNotNullArgument(dateWithTimePart, "dateWithTimePart");

        final var zone = ZoneId.systemDefault();
        final LocalDate datePart = dateWithDatePart.toInstant().atZone(zone).toLocalDate();
        final LocalTime timePart = dateWithTimePart.toInstant().atZone(zone).toLocalTime();

        return Date.from(LocalDateTime.of(datePart, timePart)
                         .atZone(zone)
                         .toInstant());
    }

    /// Computes the difference between `from` and `to` as fractional hours.
    /// The order of arguments is not important as an absolute value is returned.
    ///
    public static BigDecimal diffHours(final Date from, final Date to) {
        requireNotNullArgument(from, "from");
        requireNotNullArgument(to, "to");

        final long diffInMillis = Math.abs(to.getTime() - from.getTime());
        final double diffInHours = TimeUnit.MILLISECONDS.toMinutes(diffInMillis) / 60.0;
        return new BigDecimal(diffInHours).setScale(2, RoundingMode.HALF_EVEN);
    }

    /// Test if both `dt1` and `dt2` represent the same day, ignoring the time part.
    /// Can be conveniently used for testing if `dt1` is "today" by passing `now()` as `dt2`.
    ///
    public static boolean isSameDay(final Date dt1, final Date dt2) {
        requireNotNullArgument(dt1, "dt1");
        requireNotNullArgument(dt2, "dt2");

        final LocalDate date1 = dt1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        final LocalDate date2 = dt2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return date1.equals(date2);
    }

    /// Compares only the time portion using [LocalTime].
    /// @return  Negative value if `time(from) < time(to)`, 0 if equal, positive if `time(from) > time(to)`.
    ///
    public static int compareTimeOnly(final Date from, final Date to) {
        requireNotNullArgument(from, "from");
        requireNotNullArgument(to, "to");

        final LocalTime fromTime = from.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalTime();
        final LocalTime toTime = to.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalTime();

        return fromTime.compareTo(toTime);
    }

    /// Compares only the date portion using [LocalDate].
    /// @return  Negative value if `date(from) < date(to)`, 0 if equal, positive if `date(from) > date(to)`.
    ///
    public static int compareDateOnly(final Date from, final Date to) {
        requireNotNullArgument(from, "from");
        requireNotNullArgument(to, "to");

        final LocalDate fromDate = from.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        final LocalDate toDate = to.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        return fromDate.compareTo(toDate);
    }

}
