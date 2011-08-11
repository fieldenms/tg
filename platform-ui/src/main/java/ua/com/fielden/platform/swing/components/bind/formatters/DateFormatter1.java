package ua.com.fielden.platform.swing.components.bind.formatters;

import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.swing.text.BadLocationException;

/**
 * DateFormatter is an <code>InternationalFormatter</code> that does its formatting by way of an instance of <code>java.text.DateFormat</code>.
 * <p>
 * <strong>Warning:</strong> Serialized objects of this class will not be compatible with future Swing releases. The current serialization support is appropriate for short term
 * storage or RMI between applications running the same version of Swing. As of 1.4, support for long term storage of all JavaBeans<sup><font size="-2">TM</font></sup> has been
 * added to the <code>java.beans</code> package. Please see {@link java.beans.XMLEncoder}.
 * 
 * @see java.text.DateFormat
 * 
 * @version 1.5 04/09/01
 * @since 1.4
 */
public class DateFormatter1 extends InternationalFormatter1 {
    /**
     * This is shorthand for <code>new DateFormatter(DateFormat.getDateInstance())</code>.
     */
    public DateFormatter1() {
	this(DateFormat.getDateInstance());
    }

    /**
     * Returns a DateFormatter configured with the specified <code>Format</code> instance.
     * 
     * @param format
     *            Format used to dictate legal values
     */
    public DateFormatter1(final DateFormat format) {
	super(format);
	setFormat(format);
    }

    /**
     * Sets the format that dictates the legal values that can be edited and displayed.
     * <p>
     * If you have used the nullary constructor the value of this property will be determined for the current locale by way of the <code>Dateformat.getDateInstance()</code> method.
     * 
     * @param format
     *            DateFormat instance used for converting from/to Strings
     */
    public void setFormat(final DateFormat format) {
	super.setFormat(format);
    }

    /**
     * Returns the Calendar that <code>DateFormat</code> is associated with, or if the <code>Format</code> is not a <code>DateFormat</code> <code>Calendar.getInstance</code> is
     * returned.
     */
    protected Calendar getCalendar() {
	final Format f = getFormat();

	if (f instanceof DateFormat) {
	    return ((DateFormat) f).getCalendar();
	}
	return Calendar.getInstance();
    }

    /**
     * Returns true, as DateFormatterFilter will support incrementing/decrementing of the value.
     */
    @Override
    boolean getSupportsIncrement() {
	return true;
    }

    /**
     * Returns the field that will be adjusted by adjustValue.
     */
    @Override
    Object getAdjustField(final int start, final Map attributes) {
	final Iterator attrs = attributes.keySet().iterator();

	while (attrs.hasNext()) {
	    final Object key = attrs.next();

	    if ((key instanceof DateFormat.Field) && (key == DateFormat.Field.HOUR1 || ((DateFormat.Field) key).getCalendarField() != -1)) {
		return key;
	    }
	}
	return null;
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

		final int fieldValue = calendar.get(field);

		try {
		    calendar.add(field, direction);
		    value = calendar.getTime();
		} catch (final Throwable th) {
		    value = null;
		}
		return value;
	    }
	}
	return null;
    }
}
