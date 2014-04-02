package ua.com.fielden.platform.swing.components.bind.formatters;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import com.jgoodies.binding.BindingUtils;

/**
 * In addition to its superclass DateFormatter, this class converts to/from the empty string. Therefore it holds an <em>empty value</em> that is the counterpart of the empty
 * string. The Method <code>#valueToString</code> converts the empty value to the empty string. And <code>#stringToValue</code> converts blank strings to the empty value. In all
 * other cases the conversion is delegated to its superclass.
 * <p>
 * 
 * Often the empty value is {@code null}. As an alternative you can map the empty string to a given date, for example epoch (January 1, 1970).
 * 
 * <strong>Examples:</strong>
 * 
 * <pre>
 * new EmptyDateFormatter();
 * new EmptyDateFormatter(new Date(0));
 * </pre>
 * 
 * @author Karsten Lentzsch
 * @version $Revision: 1.6 $
 * 
 * @see java.text.Format
 */
public class EmptyDateFormatter1 extends DateFormatter1 {

    /**
     * Holds the date that is converted to an empty string and that is the result of converting blank strings to a value.
     * 
     * @see #stringToValue(String)
     * @see #valueToString(Object)
     */
    private final Date emptyValue;

    // Instance Creation ****************************************************

    /**
     * Constructs an EmptyDateFormatter that converts {@code null} to the empty string and vice versa.
     */
    public EmptyDateFormatter1() {
        this((Date) null);
    }

    /**
     * Constructs an EmptyDateFormatter configured with the specified Format; maps {@code null} to the empty string and vice versa.
     * 
     * @param format
     *            Format used to dictate legal values
     */
    public EmptyDateFormatter1(final DateFormat format) {
        this(format, null);
    }

    /**
     * Constructs an EmptyDateFormatter that converts the given <code>emptyValue</code> to the empty string and vice versa.
     * 
     * @param emptyValue
     *            the representation of the empty string
     */
    public EmptyDateFormatter1(final Date emptyValue) {
        this.emptyValue = emptyValue == null ? null : new Date(emptyValue.getTime());
    }

    /**
     * Constructs an EmptyDateFormatter configured with the specified Format; maps {@code null} to the given <code>emptyValue</code> and vice versa.
     * 
     * @param format
     *            Format used to dictate legal values
     * @param emptyValue
     *            the representation of the empty string
     */
    public EmptyDateFormatter1(final DateFormat format, final Date emptyValue) {
        super(format);
        this.emptyValue = emptyValue == null ? null : new Date(emptyValue.getTime());
    }

    // Overriding Superclass Behavior *****************************************

    /**
     * Returns the <code>Object</code> representation of the <code>String</code> <code>text</code>.
     * <p>
     * 
     * Unlike its superclass, this class converts blank strings to the empty value.
     * 
     * @param text
     *            <code>String</code> to convert
     * @return <code>Object</code> representation of text
     * @throws ParseException
     *             if there is an error in the conversion
     */
    @Override
    public Object stringToValue(final String text) throws ParseException {
        return BindingUtils.isBlank(text) ? emptyValue : super.stringToValue(text);
    }

    /**
     * Returns a String representation of the Object <code>value</code>. This invokes <code>format</code> on the current <code>Format</code>.
     * <p>
     * 
     * Unlike its superclass, this class converts the empty value to the empty string.
     * 
     * @param value
     *            the value to convert
     * @return a String representation of value
     * @throws ParseException
     *             if there is an error in the conversion
     */
    @Override
    public String valueToString(final Object value) throws ParseException {
        return BindingUtils.equals(value, emptyValue) ? "" : super.valueToString(value);
    }

}