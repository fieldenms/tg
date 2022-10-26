package ua.com.fielden.platform.entity_centre.mnemonics;

import static java.lang.String.format;
import static org.joda.time.DateTimeConstants.APRIL;
import static org.joda.time.DateTimeConstants.JANUARY;
import static org.joda.time.DateTimeConstants.JULY;
import static org.joda.time.DateTimeConstants.OCTOBER;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Date;

import org.joda.time.DateTime;

import ua.com.fielden.platform.entity_centre.exceptions.EntityCentreExecutionException;
import ua.com.fielden.platform.utils.IDates;

/**
 * Provides an utilities to work with "BEG_PREV_MONTH", "MID_CURR_YEAR" or "END_NEXT_DAY" etc. dates, which are related to "current" date.
 *
 * @author TG Team
 *
 */
public class DateMnemonicUtils {

    private DateMnemonicUtils() {}
    /**
     * Returns <code>true</code> if "rangeWidth" represents one of four quarters, otherwise returns <code>false</code>.
     *
     * @param rangeWidth
     * @return
     */
    private static boolean isQuarter(final MnemonicEnum rangeWidth) {
        return MnemonicEnum.QRT1 == rangeWidth || MnemonicEnum.QRT2 == rangeWidth || MnemonicEnum.QRT3 == rangeWidth || MnemonicEnum.QRT4 == rangeWidth;
    }

    /**
     * Returns a date that represents [beginning or middle or ending] of date range, that includes specified [date or relative "prevDate" or relative "nextDate"] with specified
     * rangeWidth [DAY or WEEK or MONTH or YEAR or QRT1..4].
     *
     * @param date
     *            - the date which is included by specified date range.
     * @param begMidEnd
     *            - represents [beginning, ending or middle] of specified date range modified by [previous current next] modifier.
     * @param prevCurrNext
     * @param rangeWidth
     * @return
     */
    public static Date dateOfRangeThatIncludes(final Date date, final DateRangeSelectorEnum begMidEnd, final DateRangePrefixEnum prevCurrNext, final MnemonicEnum rangeWidth, final IDates dates) {
        // Prev or Next quarters (quarter 1, ... quarter 4) are related to Prev or Next year, not to "neighbour" quarter. So we should use YEAR rolling for Prev/Next Quarter:
        final MnemonicEnum rollingWidth = isQuarter(rangeWidth) ? MnemonicEnum.YEAR : rangeWidth;
        final Date relativeDate = DateRangePrefixEnum.PREV == prevCurrNext ? roll(date, rollingWidth, false, dates) : //
                (DateRangePrefixEnum.CURR == prevCurrNext ? date : //
                        (DateRangePrefixEnum.NEXT == prevCurrNext ? roll(date, rollingWidth, true, dates) : null));
        return DateRangeSelectorEnum.BEGINNING == begMidEnd ? startOfDateRangeThatIncludes(relativeDate, rangeWidth, dates) : //
                (DateRangeSelectorEnum.MIDDLE == begMidEnd ? middleOfDateRangeThatIncludes(relativeDate, rangeWidth, dates) : //
                        (DateRangeSelectorEnum.ENDING == begMidEnd ? endOfDateRangeThatIncludes(relativeDate, rangeWidth, dates) : null));
    }

    /**
     * Returns a date that represents beginning of date range, that includes specified date and have rangeWidth of DAY or WEEK or MONTH or YEAR.</br>
     * {@link #startOfDateRangeThatIncludes(Date, MnemonicEnum)} <= date < {@link #endOfDateRangeThatIncludes(Date, MnemonicEnum)} </br>
     *
     * @param date
     * @param rangeWidth
     * @return
     */
    public static Date startOfDateRangeThatIncludes(final Date date, final MnemonicEnum rangeWidth, final IDates dates) {
        final DateTime zonedDate = dates.zoned(date).withTimeAtStartOfDay();

        if (rangeWidth == MnemonicEnum.DAY) {
            // time portion is already removed.
            return zonedDate.toDate();
        } else if (rangeWidth == MnemonicEnum.DAY_AND_BEFORE) {
            return null; // no left limit!
        } else if (rangeWidth == MnemonicEnum.DAY_AND_AFTER) {
            return zonedDate.toDate(); // time portion is already removed.
        } else if (rangeWidth == MnemonicEnum.WEEK) {
            // Set the first day of week, but we may need to first shift adjustedDate back by 1 week.
            // For example, an application may have the first day of the week as Sunday, which corresponds to 7 and, let's say, adjustedDate represents Tuesday (2).
            // If we simply execute adjustedDate.withDayOfWeek(7) then we will get a date in the future, corresponding to the next Sunday after the adjustedDate.
            // But what we need if the Sunday before adjustedDate. This is why we need to to first shift adjustedDate by 1 week into the past, and only then set the date of week to the shifted date.
            final DateTime newDate = zonedDate.getDayOfWeek() < dates.startOfWeek() ? zonedDate.minusWeeks(1) : zonedDate;
            return newDate.withDayOfWeek(dates.startOfWeek()).toDate();
        } else if (rangeWidth == MnemonicEnum.MONTH) {
            return zonedDate.withDayOfMonth(1).toDate();// set first day of month as 1-st.
        } else if (rangeWidth == MnemonicEnum.YEAR) {
            return zonedDate.withDayOfYear(1).toDate();// set first day of year as 1-st.
        } else if (rangeWidth == MnemonicEnum.FIN_YEAR) {
            // Set the month of year and day of month to reflect fin year, but we may need to first shift adjustedDate back by 1 year.
            // For example, an application may have FY starting on the 6th of April, and adjustedDate is the 15th of March 2022.
            // The start of the FY that in includes 15-Mar-2022 start on the 6th of April 2021. Hence, the need to shift back by 1 year.
            final int adjustedFinYearStartDay = adjustFinYearStartDayForTheCaseOfLastDayOfMonth(zonedDate, dates);
            final DateTime newDate = zonedDate.getMonthOfYear() < dates.finYearStartMonth() || 
                                     (zonedDate.getMonthOfYear() == dates.finYearStartMonth() && zonedDate.getDayOfMonth() < adjustedFinYearStartDay)
                                     ? dates.zoned(roll(zonedDate.toDate(), MnemonicEnum.YEAR, false, dates))
                                     : zonedDate;
            // The value 31 for dates.finYearStartDay() has the semantics of the "last day of the month".
            // This requires additional consideration of the length of the FY start month.
            final DateTime newDateWithFinYearStartMonth = newDate.withMonthOfYear(dates.finYearStartMonth());
            if (adjustedFinYearStartDay == 31) {
                final int lastDayOfMonth = YearMonth.from(LocalDate.of(newDateWithFinYearStartMonth.getYear(), newDateWithFinYearStartMonth.getMonthOfYear(), newDateWithFinYearStartMonth.getDayOfMonth()))
                                                     .atEndOfMonth().getDayOfMonth();
                return newDateWithFinYearStartMonth.withDayOfMonth(lastDayOfMonth).toDate();
            } else { // otherwise simply use dates.finYearStartDay() as is
                return newDateWithFinYearStartMonth.withDayOfMonth(adjustedFinYearStartDay).toDate();
            }
        } else if (rangeWidth == MnemonicEnum.QRT1) { // first quarter
            return zonedDate.withMonthOfYear(JANUARY).withDayOfMonth(1).toDate();// set first day of month as 1-st.
        } else if (rangeWidth == MnemonicEnum.QRT2) { // second quarter
            return zonedDate.withMonthOfYear(APRIL).withDayOfMonth(1).toDate();// set first day of month as 1-st.
        } else if (rangeWidth == MnemonicEnum.QRT3) { // third quarter
            return zonedDate.withMonthOfYear(JULY).withDayOfMonth(1).toDate();// set first day of month as 1-st.
        } else if (rangeWidth == MnemonicEnum.QRT4) { // fourth quarter
            return zonedDate.withMonthOfYear(OCTOBER).withDayOfMonth(1).toDate();// set first day of month as 1-st.
        } else {
            throw new EntityCentreExecutionException(format("Menmonic [%s] is not supported.", rangeWidth));
        }
    }

    /**
     * Returns an adjusted value of {@code dats.finYearStartDay()} to match the last day of {@code date}, in case {@code dats.finYearStartDay()} has the semantics of the "last day of the month",
     * and the month of {@code date} matches the {@code dats.finYearStartMonth()}.
     * <p>
     * Such adjusted value is necessary to correctly compute the need for "rolling" the date forward. 
     *
     * @param date
     * @param dates
     * @return
     */
    private static int adjustFinYearStartDayForTheCaseOfLastDayOfMonth(final DateTime date, final IDates dates) {
        if (dates.finYearStartDay() == 31 && date.getMonthOfYear() == dates.finYearStartMonth()) {
            final int dayOfMonth = YearMonth.from(LocalDate.of(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth())).atEndOfMonth().getDayOfMonth();
            return dayOfMonth;
        }
        return dates.finYearStartDay();
    }

    /**
     * Move specified "date" on specified "width" up or down.
     *
     * @param date
     * @param width
     * @param up
     * @return
     */
    private static Date roll(final Date date, final MnemonicEnum width, final boolean up, final IDates dates) {
        final DateTime old = dates.zoned(date);
        final DateTime neew = width == MnemonicEnum.DAY ? old.plusDays(up ? 1 : -1) : //
                width == MnemonicEnum.DAY_AND_BEFORE ? old.plusDays(up ? 1 : -1) : //
                        width == MnemonicEnum.DAY_AND_AFTER ? old.plusDays(up ? 1 : -1) : //
                                width == MnemonicEnum.WEEK ? old.plusWeeks(up ? 1 : -1) : //
                                        width == MnemonicEnum.MONTH ? old.plusMonths(up ? 1 : -1) : //
                                                width == MnemonicEnum.QRT1 ? old.plusMonths(up ? 3 : -3) : //
                                                        width == MnemonicEnum.QRT2 ? old.plusMonths(up ? 3 : -3) : //
                                                                width == MnemonicEnum.QRT3 ? old.plusMonths(up ? 3 : -3) : //
                                                                        width == MnemonicEnum.QRT4 ? old.plusMonths(up ? 3 : -3) : //
                                                                                width == MnemonicEnum.YEAR ? old.plusYears(up ? 1 : -1) : //
                                                                                        width == MnemonicEnum.FIN_YEAR ? old.plusYears(up ? 1 : -1) : null;
        if (neew == null) {
            throw new RuntimeException("Incorrect MnemonicEnum specified.");
        }
        return neew.toDate();
    }

    /**
     * Returns a date that represents ending of date range (that includes specified date and have rangeWidth of DAY or WEEK or MONTH or YEAR ).
     * <p>
     * <b>Important</b>: the range does not include this ending! 
     * <br>
     * {@link #startOfDateRangeThatIncludes(Date, MnemonicEnum)} <= date < {@link #endOfDateRangeThatIncludes(Date, MnemonicEnum)}
     *
     * @param date
     * @param rangeWidth
     * @return
     */
    public static Date endOfDateRangeThatIncludes(final Date date, final MnemonicEnum rangeWidth, final IDates dates) {
        return (MnemonicEnum.DAY_AND_AFTER == rangeWidth) ? null : //
                (MnemonicEnum.DAY_AND_BEFORE == rangeWidth) ? startOfDateRangeThatIncludes(roll(date, MnemonicEnum.DAY, true, dates), MnemonicEnum.DAY, dates) : //
                        (isQuarter(rangeWidth) ? roll(startOfDateRangeThatIncludes(date, rangeWidth, dates), rangeWidth, true, dates) : //
                                startOfDateRangeThatIncludes(roll(date, rangeWidth, true, dates), rangeWidth, dates));
    }

    /**
     * Returns a date that represents middle of date range, that includes specified date and have rangeWidth of DAY or WEEK or MONTH or YEAR.</br>
     * {@link #startOfDateRangeThatIncludes(Date, MnemonicEnum)} <= date < {@link #endOfDateRangeThatIncludes(Date, MnemonicEnum)} </br>
     *
     * @param date
     * @param rangeWidth
     * @return
     */
    public static Date middleOfDateRangeThatIncludes(final Date date, final MnemonicEnum rangeWidth, final IDates dates) {
        return new Date((startOfDateRangeThatIncludes(date, rangeWidth, dates).getTime() + endOfDateRangeThatIncludes(date, rangeWidth, dates).getTime()) / 2);
    }

    /**
     * Creates {@link DateTime} instance from <code>date</code> instance with respect of returning <code>null</code> for <code>null</code> <code>date</code>.
     *
     * @param date
     * @return
     */
    public static DateTime convert(final Date date, final IDates dates) {
        return date == null ? null : dates.zoned(date);
    }

    /**
     * Creates {@link Date} instance from <code>dateTime</code> instance with respect of returning <code>null</code> for <code>null</code> <code>dateTime</code>.
     *
     * @param dateTime
     * @return
     */
    public static Date convert(final DateTime dateTime) {
        return dateTime == null ? null : dateTime.toDate();
    }

    /**
     * Returns a minimum date from two values with respect of <code>null</code> value (which means +infinity).
     *
     * @param one
     * @param sec
     * @return
     */
    public static DateTime min(final DateTime one, final DateTime sec) {
        return isBefore(one, sec) ? one : sec;
    }

    /**
     * Returns a maximum date from two values with respect of <code>null</code> value (which means +infinity).
     *
     * @param one
     * @param sec
     * @return
     */
    public static DateTime max(final DateTime one, final DateTime sec) {
        return isBefore(one, sec) ? sec : one;
    }

    /**
     * Returns <code>true</code> if "one isBefore sec" (<code>false</code> otherwise) with respect of <code>null</code> value (which means +infinity).
     *
     * @param one
     * @param sec
     * @return
     */
    public static boolean isBefore(final DateTime one, final DateTime sec) {
        return one != null && (sec == null || one.isBefore(sec));
    }
}
