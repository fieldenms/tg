package ua.com.fielden.platform.example.swing.components.smart.datepicker;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXDatePicker;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.basic.autocompleter.PojoValueMatcher;
import ua.com.fielden.platform.example.swing.components.autocompleter.DemoEntity;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.components.smart.autocompleter.development.AutocompleterTextFieldLayer;
import ua.com.fielden.platform.swing.components.smart.autocompleter.renderer.development.MultiplePropertiesListCellRenderer;
import ua.com.fielden.platform.swing.components.smart.datepicker.DatePickerLayer;
import ua.com.fielden.platform.swing.components.textfield.UpperCaseTextField;
import ua.com.fielden.platform.swing.components.textfield.caption.CaptionTextFieldLayer;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;

public class DatePickerLayerDemo {
    public static void main(final String[] args) throws Exception {
	for (final LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
	    if ("Nimbus".equals(laf.getName())) {
		UIManager.setLookAndFeel(laf.getClassName());
	    }
	}
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		final JPanel panel = new JPanel(new MigLayout("fill"));
		panel.add(createAutocompleter(), "growx, h 25!, w 300, wrap");
		final JXDatePicker picker = new JXDatePicker(Locale.TRADITIONAL_CHINESE);
		final Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, 10);
		picker.getMonthView().setFlaggedDayForeground(Color.green);
		picker.getMonthView().setFlaggedDates(new Date(), c.getTime());
		picker.getMonthView().setTodayBackground(Color.MAGENTA);
		panel.add(picker, "growx, h 25!, w 300, wrap");
		panel.add(new DatePickerLayer("type date...", Locale.getDefault(), true, new Date(), DatePickerLayer.defaultTimePortionMillisForTheEndOfDay()), "growx, h 25!, w 300, wrap");
		panel.add(new DatePickerLayer("type date...", Locale.TRADITIONAL_CHINESE, true, null, 0L), "growx, h 25!, w 300, wrap");
		panel.add(new CaptionTextFieldLayer<JTextField>(new JFormattedTextField(), "some caption"), "growx, h 25!, w 300, wrap");
		panel.add(new JButton(new Command<Boolean>("Print values") {
		    @Override
		    protected Boolean action(final ActionEvent e) throws Exception {
			//			for (final DemoEnum entity : autocompleter.values()) {
			//			    System.out.println(entity);
			//			}
			return true;
		    }
		}), "align right");

		panel.add(new JLabel("bla"));
		final JFormattedTextField tft3 = new JFormattedTextField(new SimpleDateFormat("yyyy-M-d"));
		tft3.setValue(new Date());
		panel.add(tft3);


		SimpleLauncher.show("Date picker layer demo", panel);

	    }
	});
    }

    private static AutocompleterTextFieldLayer<DemoEntity> createAutocompleter(){
	// Property <code>values</code> holds a complete list of values used for autocompletion. Alternative implementations may retrieve data from a database or other sources.
	final DemoEntity[] acceptableValues = new DemoEntity[] { new DemoEntity("NAME 1", "demo for name 1 demo for name 1 demo for name 1"),
		new DemoEntity("NAME 2", "demo for name 2"), new DemoEntity("NAME 3", "demo for name 3"), new DemoEntity("NMAE", "demo for name 2"),
		new DemoEntity("DONE 1", "demo for name 3"), new DemoEntity("D2NE 2", "demo for name 3"), new DemoEntity("DONE 3", "demo for name 3") };

	// create an instance of the overlayable text field, which bill be used for attaching overlay components
	final IValueMatcher<DemoEntity> matcher = new PojoValueMatcher<DemoEntity>(Arrays.asList(acceptableValues), "name", 10) {
	    @Override
	    public List<DemoEntity> findMatches(final String value) {
		try {
		    Thread.sleep(1000);
		} catch (final InterruptedException e) {
		}

		return super.findMatches(value);
	    }
	};

	final MultiplePropertiesListCellRenderer<DemoEntity> cellRenderer = new MultiplePropertiesListCellRenderer<DemoEntity>("name", "desc");
	final AutocompleterTextFieldLayer<DemoEntity> autocompleter = new AutocompleterTextFieldLayer<DemoEntity>(new UpperCaseTextField(), matcher, DemoEntity.class, "name", cellRenderer, "caption...", ";");
	cellRenderer.setAuto(autocompleter.getAutocompleter());
	return autocompleter;
    }
}
