package ua.com.fielden.platform.swing.components.bind.formatters;

import java.text.DateFormat;
import java.text.DateFormat.Field;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.swing.JFormattedTextField;
import javax.swing.text.BadLocationException;

import org.jdesktop.swingx.calendar.DateUtils;

import ua.com.fielden.platform.swing.components.bind.development.BoundedJXDatePicker;

/**
 * This is EmptyDateFormatter descendant overridden in order to support "time portion" independence from entire date. "Independence" means that when we manipulate time portion
 * using arrow keys (in formatted text field) - "date portion" will not change.
 * 
 * @author Jhou
 * 
 */
public class EmptyDateFormatterWithIndependentTimePortion extends EmptyDateFormatter1 {
    private static final long serialVersionUID = -6162486184519891524L;

    public EmptyDateFormatterWithIndependentTimePortion(final DateFormat editDateFormat, final Date emptyValue) {
        super(editDateFormat, emptyValue);
    }

    /** Overidden in order to take a cursor into appropriate position. In this case it will be moved to time portion beginning if it exists. */
    @Override
    public void install(final JFormattedTextField ftf) {
        // final int prevLen = ftf.getDocument().getLength();
        // final int savedCaretPos = ftf.getCaretPosition();
        super.install(ftf);
        // if (ftf.getDocument().getLength() == prevLen) {
        //     ftf.setCaretPosition(savedCaretPos);
        // }
        final int timePortionPosition = 11; // 11 is a length of the longest "DD/MM/YYYY " portion in default date pattern, after which we should see time portion.
        if (ftf.getDocument().getLength() > timePortionPosition) {
            ftf.setCaretPosition(timePortionPosition);
        }
    }

    /**
     * Returns true if <code>field</code> represents "time" constants, not "date".
     * 
     * @param field
     */
    private static boolean isTimeField(final Field field) {
        final boolean isTimeField = Field.HOUR_OF_DAY1.equals(field) || Field.HOUR_OF_DAY0.equals(field) || Field.MINUTE.equals(field) || Field.SECOND.equals(field)
                || Field.MILLISECOND.equals(field) || Field.AM_PM.equals(field) || Field.HOUR1.equals(field) || Field.HOUR0.equals(field);
        return isTimeField;
    }

    /**
     * Adjusts the Date if FieldPosition identifies a known calendar field.
     */
    @Override
    Object adjustValue(Object value, final Map attributes, Object key, final int direction) throws BadLocationException, ParseException {
        if (key != null) {
            int field;

            // HOUR1 has no corresponding calendar field, thus, map
            // it to HOUR0 which will give the correct behavior.
            if (key == DateFormat.Field.HOUR1) {
                key = DateFormat.Field.HOUR0;
            }
            field = ((DateFormat.Field) key).getCalendarField();

            final Calendar calendar = getCalendar();

            if (calendar != null) {
                calendar.setTime((Date) value);
                try {
                    if (!isTimeField((DateFormat.Field) key)) {
                        calendar.add(field, direction);
                    } else {
                        calendar.setTime(changeTimeWithoutAffectingDatePortion(calendar.getTime(), field, direction));
                    }
                    value = calendar.getTime();
                } catch (final Throwable th) {
                    value = null;
                }
                return value;
            }
        }
        return null;
    }

    /**
     * Changes calendar's time without affecting date portion.
     * 
     * @param calendar
     *            - calendar to adjust
     * @param field
     *            - time field, for e.g. HOUR1, MILLISECOND etc.
     * @param direction
     *            - increment value
     */
    private Date changeTimeWithoutAffectingDatePortion(final Date date, final int field, final int direction) {
        final Calendar calendar = getCalendar();
        calendar.setTime(date);
        final long oldMillisInDatePortion = DateUtils.startOfDay(calendar.getTime()).getTime(); // calendar.getTimeInMillis() - BoundedJXDatePicker.millisOfTheDateDay(calendar.getTime());
        calendar.add(field, direction);
        final long newMillisInTimePortion = BoundedJXDatePicker.timePortionMillis(calendar.getTime());
        calendar.setTimeInMillis(oldMillisInDatePortion + newMillisInTimePortion);
        return calendar.getTime();
    }
}