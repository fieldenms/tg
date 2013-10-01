package ua.com.fielden.platform.swing.components.smart.datepicker;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.jdesktop.swingx.util.Contract;

import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.SpecialFormattedField;
import ua.com.fielden.platform.swing.components.smart.development.SmartComponentUi;
import ua.com.fielden.platform.utils.ConverterFactory;

/**
 * Implements Ui logic for date picker layer.
 *
 * @author Jhou
 *
 */
public class DatePickerUi extends SmartComponentUi<JFormattedTextField, DatePickerLayer> {

    public DatePickerUi(final DatePickerLayer layer, final String caption) {
	super(layer, caption, true);
    }

    @Override
    public boolean isHintsPopupVisible() {
	return getLayer().getDatePickerLogic().isHintsPopupVisible();
    }

    @Override
    public void showHintsPopup() {
	getLayer().getDatePickerLogic().showHints();
    }

    @Override
    public void performAcceptAction() {
	getLayer().getDatePickerLogic().performAcceptAction(getLayer().getDatePickerLogic());
    }

    public static void main(final String[] args) {
	final Date d = new Date();
	final Calendar c = Calendar.getInstance();
	c.setTime(d);
	System.out.println(c.get(Calendar.YEAR));
    }

    /**
     * Creates the editor used to edit the date selection. The editor is configured with the default DatePickerFormatter marked as UIResource.
     *
     * @return an instance of a JFormattedTextField
     */
    protected static JFormattedTextField createEditor(final Locale locale, final boolean useTimePortion, final Long defaultTimePortionMillis) {
	final DateFormat fullFormat = convertFormat(locale, ConverterFactory.createFullDateFormat(locale)),
			 dateAndPartialTimeFormat = convertFormat(locale, ConverterFactory.createShortDateAndHoursAndMinutesFormat(locale)),
			 dateAndPartialTimeWithoutMinutesFormat = convertFormat(locale, ConverterFactory.createShortDateAndHoursFormat(locale)),
			 dateFormat = convertFormat(locale, ConverterFactory.createShortDateFormat(locale));
	final AbstractFormatterFactory fullFactory = ComponentFactory.createDateFormatterFactory(fullFormat, fullFormat),
				       dateAndPartialTimeFactory = ComponentFactory.createDateFormatterFactory(dateAndPartialTimeFormat, dateAndPartialTimeFormat),
				       dateAndPartialTimeWithoutMinutesFactory = ComponentFactory.createDateFormatterFactory(dateAndPartialTimeWithoutMinutesFormat, dateAndPartialTimeWithoutMinutesFormat),
				       dateFactory = ComponentFactory.createDateFormatterFactory(dateFormat, dateFormat);

	final JFormattedTextField f = new SpecialFormattedField(useTimePortion ? fullFactory : dateFactory) {
	    private static final long serialVersionUID = 892493264832644324L;

	    private void setValueSilently(final Object value) {
		if (getFormatterFactory() == null){
		    throw new RuntimeException("Formatter factory should be explicitly provided for date picker layer field.");
		}
		// Actually we just need to call setValue(value, false, true). But this method is private.
		// So we have to override setFormatter() method not to invoke "formatter re-setting" in this case.
		silentValueSetting = true;
		if (value != null) {
		    final Date date = (Date) value;
		    final Calendar c = Calendar.getInstance();
		    c.setTime(date);
		    final int year = c.get(Calendar.YEAR);
		    if (year <= 99 && year >= 0) {
			c.set(Calendar.YEAR, (year <= 50) ? (2000 + year) : (1900 + year)); // use partial year typed as [0-50]=>[2000-2050] and [51-99]=>[1951-1999]
			setValue(c.getTime());
		    } else {
			setValue(value); // false, true
		    }
		} else {
		    setValue(value); // false, true
		}
	        silentValueSetting = false;
	    }

	    private boolean silentValueSetting = false;

	    @Override
	    protected void setFormatter(final AbstractFormatter format) {
		if (!silentValueSetting){
		    super.setFormatter(format);
		}
	    }

	    @Override
	    public void commitEdit() throws ParseException {
		if (useTimePortion){
		    try {
			// try "dd/MM/yyyy hh:mma" :
			final AbstractFormatter format = fullFactory.getFormatter(this); // super.commitEdit();
			if (format != null) {
			    setValueSilently(format.stringToValue(getText()));
			}
		    } catch (final ParseException e1) {
			try {
			    // in case of full format failure, try "dd/MM/yyyy hh:mm" :
			    final AbstractFormatter format = dateAndPartialTimeFactory.getFormatter(this);
			    if (format != null) {
				setValueSilently(format.stringToValue(getText()));
			    }
			} catch (final ParseException e2) {
			    try {
				// in case of full and "dd/MM/yyyy hh:mm" formats failures, try "dd/MM/yyyy hh" :
				final AbstractFormatter format = dateAndPartialTimeWithoutMinutesFactory.getFormatter(this);
				if (format != null) {
				    setValueSilently(format.stringToValue(getText()));
				}
			    } catch (final ParseException e3) {
				// in case of full and "dd/MM/yyyy hh:mm" and "dd/MM/yyyy hh" formats failures, try simple "dd/MM/yyyy".
				// note that "12/10/2010sdjfyudisfyie" string is correct!
				final AbstractFormatter format = dateFactory.getFormatter(this);
				if (format != null) {
				    try {
					final Date value = (Date) format.stringToValue(getText()); // a value of date retrieved directly from date picker (e.g. 12/12/2001)
					final Date modifiedByDefaultTimePortionDate = new Date(value.getTime() + defaultTimePortionMillis);
					setValueSilently(modifiedByDefaultTimePortionDate);
				    } catch (final ParseException e4) {
					throw e4;
				    }
				}
			    }
			}
		    }
		} else {
		    super.commitEdit();
		}
	    }
	};

	f.setName("dateField");
	f.setLocale(locale);
	// this produces a fixed pref widths, looking a bit funny
	// int columns = UIManagerExt.getInt("JXDatePicker.numColumns", null);
	// if (columns > 0) {
	// f.setColumns(columns);
	// }
	// that's always 0 as it comes from the resourcebundle
	// f.setColumns(UIManager.getInt("JXDatePicker.numColumns"));
	final Border border = UIManager.getBorder("JXDatePicker.border");
	if (border != null) {
	    f.setBorder(border);
	}
	return f;
    }

    /**
     * Converts string representation of format to <code>DateFormat</code> representation.
     *
     * @param stringsFormat
     * @return
     */
    public static DateFormat defaultFormat() {
	return convertFormat(Locale.getDefault(), "dd/MM/yyyy hh:mma");
    }

    /**
     * Converts string representation of format to <code>DateFormat</code> representation.
     *
     * @param stringsFormat
     * @return
     */
    public static DateFormat convertFormat(final Locale locale, final String format) {
	return convertFormats(locale, format)[0];
    }

    /**
     * Converts string representations of formats to <code>DateFormat</code> representations.
     *
     * @param stringsFormats
     * @return
     */
    private static DateFormat[] convertFormats(final Locale locale, final String... formats) {
	DateFormat[] dateFormats = null;
	if (formats != null) {
	    Contract.asNotNull(formats, "the array of format strings must not " + "must not contain null elements");
	    dateFormats = new DateFormat[formats.length];
	    for (int counter = formats.length - 1; counter >= 0; counter--) {
		dateFormats[counter] = new SimpleDateFormat(formats[counter], locale);
	    }
	}
	return dateFormats;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
