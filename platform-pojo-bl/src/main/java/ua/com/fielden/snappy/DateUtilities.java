package ua.com.fielden.snappy;

import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;

/**
 * Provides an utilities to work with "BEG_PREV_MONTH", "MID_CURR_YEAR" or "END_NEXT_DAY" etc. dates, which are related to "current" date.
 *
 * @author Jhou
 *
 */
public class DateUtilities {

    /**
     * Returns <code>true</code> if "rangeWidth" represents one of four quarters, otherwise returns <code>false</code>.
     *
     * @param rangeWidth
     * @return
     */
    private boolean isQuarter(final MnemonicEnum rangeWidth){
	return MnemonicEnum.QRT1 == rangeWidth || MnemonicEnum.QRT2 == rangeWidth || MnemonicEnum.QRT3 == rangeWidth || MnemonicEnum.QRT4 == rangeWidth;
    }

    /**
     * Returns a date that represents [beginning or middle or ending] of date
     * range, that includes specified [date or relative "prevDate" or relative
     * "nextDate"] with specified rangeWidth [DAY or WEEK or MONTH or YEAR or QRT1..4].
     *
     * @param date
     *            - the date which is included by specified date range.
     * @param begMidEnd
     *            - represents [beginning, ending or middle] of specified date
     *            range modified by [previous current next] modifier.
     * @param prevCurrNext
     * @param rangeWidth
     * @return
     */
    public Date dateOfRangeThatIncludes(final Date date, final DateRangeSelectorEnum begMidEnd, final DateRangePrefixEnum prevCurrNext, final MnemonicEnum rangeWidth) {
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
     * Returns a date that represents beginning of date range, that includes
     * specified date and have rangeWidth of DAY or WEEK or MONTH or YEAR.</br>
     * {@link #startOfDateRangeThatIncludes(Date, MnemonicEnum)} <= date <
     * {@link #endOfDateRangeThatIncludes(Date, MnemonicEnum)} </br>
     *
     * @param date
     * @param rangeWidth
     * @return
     */
    public Date startOfDateRangeThatIncludes(final Date date, final MnemonicEnum rangeWidth) {
	final Calendar c = Calendar.getInstance();
	c.setTime(date); // exact day instance with time portion.
	// remove time portion:
	c.set(Calendar.HOUR_OF_DAY, 0);
	c.set(Calendar.MINUTE, 0);
	c.set(Calendar.SECOND, 0);
	c.set(Calendar.MILLISECOND, 0);

	if (rangeWidth == MnemonicEnum.DAY) {
	    // time portion is already removed.
	} else if (rangeWidth == MnemonicEnum.DAY_AND_BEFORE) {
	    return null; // no left limit!
	} else if (rangeWidth == MnemonicEnum.DAY_AND_AFTER) {
	    // time portion is already removed.
	} else if (rangeWidth == MnemonicEnum.WEEK) {
	    c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek()); // set the first day of week as in current locale.
	} else if (rangeWidth == MnemonicEnum.MONTH) {
	    c.set(Calendar.DAY_OF_MONTH, 1);// set first day of month as 1-st.
	} else if (rangeWidth == MnemonicEnum.YEAR) {
	    c.set(Calendar.DAY_OF_YEAR, 1);// set first day of year as 1-st.
	} else if (rangeWidth == MnemonicEnum.OZ_FIN_YEAR) {
	    if (c.get(Calendar.MONTH) < Calendar.JULY){
		final Date d = c.getTime();
		final Date newDate = roll(d, MnemonicEnum.YEAR, false);
		c.setTime(newDate);
	    }
	    c.set(Calendar.MONTH, Calendar.JULY);
	    c.set(Calendar.DAY_OF_MONTH, 1);// set first day of month as 1-st.
	} else if (rangeWidth == MnemonicEnum.QRT1) { // first quarter
	    c.set(Calendar.MONTH, Calendar.JANUARY);
	    c.set(Calendar.DAY_OF_MONTH, 1);// set first day of month as 1-st.
	} else if (rangeWidth == MnemonicEnum.QRT2) { // second quarter
	    c.set(Calendar.MONTH, Calendar.APRIL);
	    c.set(Calendar.DAY_OF_MONTH, 1);// set first day of month as 1-st.
	} else if (rangeWidth == MnemonicEnum.QRT3) { // third quarter
	    c.set(Calendar.MONTH, Calendar.JULY);
	    c.set(Calendar.DAY_OF_MONTH, 1);// set first day of month as 1-st.
	} else if (rangeWidth == MnemonicEnum.QRT4) { // fourth quarter
	    c.set(Calendar.MONTH, Calendar.OCTOBER);
	    c.set(Calendar.DAY_OF_MONTH, 1);// set first day of month as 1-st.
	} else {
	    throw new RuntimeException("Incorrect MnemonicEnum specified.");
	}
	return c.getTime();
    }

    /**
     * Move specified "date" on specified "width" up or down.
     *
     * @param date
     * @param width
     * @param up
     * @return
     */
    private Date roll(final Date date, final MnemonicEnum width, final boolean up) {
	final DateTime old = new DateTime(date);
	final DateTime neew = 	width == MnemonicEnum.DAY ? old.plusDays(up ? 1 : -1) : //
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
	if (neew == null){
	    throw new RuntimeException("Incorrect MnemonicEnum specified.");
	}
	return neew.toDate();
    }

    /**
     * Returns a date that represents ending of date range( that includes
     * specified date and have rangeWidth of DAY or WEEK or MONTH or YEAR ).
     * Important : the range does not include this ending! </br>
     * {@link #startOfDateRangeThatIncludes(Date, MnemonicEnum)} <= date <
     * {@link #endOfDateRangeThatIncludes(Date, MnemonicEnum)} </br>
     *
     * @param date
     * @param rangeWidth
     * @return
     */
    public Date endOfDateRangeThatIncludes(final Date date, final MnemonicEnum rangeWidth) {
	return  (MnemonicEnum.DAY_AND_AFTER == rangeWidth) ? null : //
	    	(MnemonicEnum.DAY_AND_BEFORE == rangeWidth) ? startOfDateRangeThatIncludes(roll(date, MnemonicEnum.DAY, true), MnemonicEnum.DAY) : //
	    	(isQuarter(rangeWidth) ? roll(startOfDateRangeThatIncludes(date, rangeWidth), rangeWidth, true) : //
		startOfDateRangeThatIncludes(roll(date, rangeWidth, true), rangeWidth));
    }

    /**
     * Returns a date that represents middle of date range, that includes
     * specified date and have rangeWidth of DAY or WEEK or MONTH or YEAR.</br>
     * {@link #startOfDateRangeThatIncludes(Date, MnemonicEnum)} <= date <
     * {@link #endOfDateRangeThatIncludes(Date, MnemonicEnum)} </br>
     *
     * @param date
     * @param rangeWidth
     * @return
     */
    public Date middleOfDateRangeThatIncludes(final Date date, final MnemonicEnum rangeWidth) {
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
        return one == null ? false : (sec == null ? true : one.isBefore(sec));
    }

}
