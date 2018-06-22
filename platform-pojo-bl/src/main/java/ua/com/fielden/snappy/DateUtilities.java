package ua.com.fielden.snappy;

import static org.joda.time.DateTimeConstants.APRIL;
import static org.joda.time.DateTimeConstants.JANUARY;
import static org.joda.time.DateTimeConstants.JULY;
import static org.joda.time.DateTimeConstants.MONDAY;
import static org.joda.time.DateTimeConstants.OCTOBER;

import java.util.Date;

import org.joda.time.DateTime;

/**
 * Provides an utilities to work with "BEG_PREV_MONTH", "MID_CURR_YEAR" or "END_NEXT_DAY" etc. dates, which are related to "current" date.
 * 
 * @author TG Team
 * 
 */
public class DateUtilities {

    private DateUtilities() {}
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
    public static Date dateOfRangeThatIncludes(final Date date, final DateRangeSelectorEnum begMidEnd, final DateRangePrefixEnum prevCurrNext, final MnemonicEnum rangeWidth) {
        // Prev or Next quarters (quarter 1, ... quarter 4) are related to Prev or Next year, not to "neighbour" quarter. So we should use YEAR rolling for Prev/Next Quarter:
        final MnemonicEnum rollingWidth = isQuarter(rangeWidth) ? MnemonicEnum.YEAR : rangeWidth;
        final Date relativeDate = DateRangePrefixEnum.PREV == prevCurrNext ? roll(date, rollingWidth, false) : //
                (DateRangePrefixEnum.CURR == prevCurrNext ? date : //
                        (DateRangePrefixEnum.NEXT == prevCurrNext ? roll(date, rollingWidth, true) : null));
        return DateRangeSelectorEnum.BEGINNING == begMidEnd ? startOfDateRangeThatIncludes(relativeDate, rangeWidth) : //
                (DateRangeSelectorEnum.MIDDLE == begMidEnd ? middleOfDateRangeThatIncludes(relativeDate, rangeWidth) : //
                        (DateRangeSelectorEnum.ENDING == begMidEnd ? endOfDateRangeThatIncludes(relativeDate, rangeWidth) : null));
    }

    /**
     * Returns a date that represents beginning of date range, that includes specified date and have rangeWidth of DAY or WEEK or MONTH or YEAR.</br>
     * {@link #startOfDateRangeThatIncludes(Date, MnemonicEnum)} <= date < {@link #endOfDateRangeThatIncludes(Date, MnemonicEnum)} </br>
     * 
     * @param date
     * @param rangeWidth
     * @return
     */
    public static Date startOfDateRangeThatIncludes(final Date date, final MnemonicEnum rangeWidth) {
        final DateTime adjustedDate = new DateTime(date).withTimeAtStartOfDay();

        if (rangeWidth == MnemonicEnum.DAY) {
            // time portion is already removed.
            return adjustedDate.toDate();
        } else if (rangeWidth == MnemonicEnum.DAY_AND_BEFORE) {
            return null; // no left limit!
        } else if (rangeWidth == MnemonicEnum.DAY_AND_AFTER) {
            return adjustedDate.toDate(); // time portion is already removed.
        } else if (rangeWidth == MnemonicEnum.WEEK) {
            // set the first day of week as MONDAY regardless of the current locale.
            return adjustedDate.withDayOfWeek(MONDAY).toDate();
        } else if (rangeWidth == MnemonicEnum.MONTH) {
            return adjustedDate.withDayOfMonth(1).toDate();// set first day of month as 1-st.
        } else if (rangeWidth == MnemonicEnum.YEAR) {
            return adjustedDate.withDayOfYear(1).toDate();// set first day of year as 1-st.
        } else if (rangeWidth == MnemonicEnum.OZ_FIN_YEAR) {
            final DateTime newDate = (adjustedDate.getMonthOfYear() < JULY) ? new DateTime(roll(adjustedDate.toDate(), MnemonicEnum.YEAR, false)) : adjustedDate;
            return newDate.withMonthOfYear(JULY).withDayOfMonth(1).toDate();// set first day of month as 1-st.
        } else if (rangeWidth == MnemonicEnum.QRT1) { // first quarter
            return adjustedDate.withMonthOfYear(JANUARY).withDayOfMonth(1).toDate();// set first day of month as 1-st.
        } else if (rangeWidth == MnemonicEnum.QRT2) { // second quarter
            return adjustedDate.withMonthOfYear(APRIL).withDayOfMonth(1).toDate();// set first day of month as 1-st.
        } else if (rangeWidth == MnemonicEnum.QRT3) { // third quarter
            return adjustedDate.withMonthOfYear(JULY).withDayOfMonth(1).toDate();// set first day of month as 1-st.
        } else if (rangeWidth == MnemonicEnum.QRT4) { // fourth quarter
            return adjustedDate.withMonthOfYear(OCTOBER).withDayOfMonth(1).toDate();// set first day of month as 1-st.
        } else {
            throw new RuntimeException("Incorrect MnemonicEnum specified.");
        }
    }

    /**
     * Move specified "date" on specified "width" up or down.
     * 
     * @param date
     * @param width
     * @param up
     * @return
     */
    private static Date roll(final Date date, final MnemonicEnum width, final boolean up) {
        final DateTime old = new DateTime(date);
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
                                                                                        width == MnemonicEnum.OZ_FIN_YEAR ? old.plusYears(up ? 1 : -1) : null;
        if (neew == null) {
            throw new RuntimeException("Incorrect MnemonicEnum specified.");
        }
        return neew.toDate();
    }

    /**
     * Returns a date that represents ending of date range( that includes specified date and have rangeWidth of DAY or WEEK or MONTH or YEAR ). Important : the range does not
     * include this ending! </br> {@link #startOfDateRangeThatIncludes(Date, MnemonicEnum)} <= date < {@link #endOfDateRangeThatIncludes(Date, MnemonicEnum)} </br>
     * 
     * @param date
     * @param rangeWidth
     * @return
     */
    public static Date endOfDateRangeThatIncludes(final Date date, final MnemonicEnum rangeWidth) {
        return (MnemonicEnum.DAY_AND_AFTER == rangeWidth) ? null : //
                (MnemonicEnum.DAY_AND_BEFORE == rangeWidth) ? startOfDateRangeThatIncludes(roll(date, MnemonicEnum.DAY, true), MnemonicEnum.DAY) : //
                        (isQuarter(rangeWidth) ? roll(startOfDateRangeThatIncludes(date, rangeWidth), rangeWidth, true) : //
                                startOfDateRangeThatIncludes(roll(date, rangeWidth, true), rangeWidth));
    }

    /**
     * Returns a date that represents middle of date range, that includes specified date and have rangeWidth of DAY or WEEK or MONTH or YEAR.</br>
     * {@link #startOfDateRangeThatIncludes(Date, MnemonicEnum)} <= date < {@link #endOfDateRangeThatIncludes(Date, MnemonicEnum)} </br>
     * 
     * @param date
     * @param rangeWidth
     * @return
     */
    public static Date middleOfDateRangeThatIncludes(final Date date, final MnemonicEnum rangeWidth) {
        return new Date((startOfDateRangeThatIncludes(date, rangeWidth).getTime() + endOfDateRangeThatIncludes(date, rangeWidth).getTime()) / 2);
    }

    /**
     * Creates {@link DateTime} instance from <code>date</code> instance with respect of returning <code>null</code> for <code>null</code> <code>date</code>.
     * 
     * @param date
     * @return
     */
    public static DateTime convert(final Date date) {
        return date == null ? null : new DateTime(date);
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
