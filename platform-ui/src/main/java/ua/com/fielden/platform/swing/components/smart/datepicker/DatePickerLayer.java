package ua.com.fielden.platform.swing.components.smart.datepicker;

import java.awt.Color;
import java.util.Date;
import java.util.Locale;

import javax.swing.JFormattedTextField;

import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.swingx.calendar.DateUtils;

/**
 * Date text field layer with capability of choosing date from JXMonthView popup. Contains "smart" button (similar as autocompleter contains) that invokes date chooser popup.
 *
 * @author Jhou
 *
 */
public class DatePickerLayer extends JXLayer<JFormattedTextField> {
    private static final long serialVersionUID = 1L;

    private final Long defaultTimePortionMillis;
    private final DatePickerLogic datePickerLogic;

    /**
     * Returns a value of default time-portion millis indicating the end of the day.
     *
     * @return
     */
    public static Long defaultTimePortionMillisForTheEndOfDay(){
	final Date now = new Date();
	return DateUtils.endOfDay(now).getTime() - DateUtils.startOfDay(now).getTime();
    }

    /**
     * Creates date picker layer.
     *
     * @param caption
     *            - the caption to be displayed when date picker's value is empty/null.
     * @param locale
     *            - localization parameter that defines month view and editor languages and other localization properties.
     * @param useTimePortion
     *            - <code>true</code> to contain (and enable to edit) "time portion" in editor, <code>false</code> - otherwise.
     * @param selectedDate
     *            - initial date.
     *
     * @param defaultTimePortionMillis - defines a default value of time-portion millis to be used when picking-up/typing a brand new date; if previous date has non-empty time-portion millis - newDate will be altered by them (default value will be ignored).
     */
    public DatePickerLayer(final String caption, final Locale locale, final boolean useTimePortion, final Date selectedDate, final Long defaultTimePortionMillis) {
	super(DatePickerUi.createEditor(locale, useTimePortion, defaultTimePortionMillis));

	this.defaultTimePortionMillis = defaultTimePortionMillis;

	if (locale != null) {
	    setLocale(locale);
	}

	datePickerLogic = new DatePickerLogic(this, locale, defaultTimePortionMillis);

	// instantiates UI and assigns it to this layer
	new DatePickerUi(this, caption);

	//	addFocusListener(new FocusAdapter() {
	//	    @Override
	//	    public void focusGained(final FocusEvent e) {
	//		getView().requestFocusInWindow();
	//	    }
	//	});

	// prevents grey background color of text field behind layer
	getView().setBackground(Color.white);

	setDate(selectedDate);
    }

    public DatePickerUi getUi() {
	return (DatePickerUi) super.getUI();
    }

    /**
     * Sets date picker layer's date.
     *
     * @param selected
     */
    public void setDate(final Date selected) {
	(getView()).setValue(selected);
    }

    /**
     * Returns datePicker's selected date. Note : all "selected dates" in month view and formatted field (and other possible date picker sub-components) should be synchronized.
     *
     * @return
     */
    public Date getDate() {
	return (Date) (getView()).getValue();
    }

    public void setEditable(final boolean flag) {
	getView().setEditable(flag);
    }

    public DatePickerLogic getDatePickerLogic() {
	return datePickerLogic;
    }

    public Long getDefaultTimePortionMillis() {
        return defaultTimePortionMillis;
    }

}
