package ua.com.fielden.platform.swing.components;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Calendar;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

import org.jfree.ui.RefineryUtilities;
import org.joda.time.DateTime;

import com.jidesoft.combobox.DateComboBox;
import com.jidesoft.combobox.DefaultDateModel;
import com.jidesoft.plaf.LookAndFeelFactory;

/**
 * Control for specifying date range start and finish using two date pickers.
 * 
 * @author nc
 * 
 */
public class DateRange {
    private static final long serialVersionUID = 1L;

    public final DateComboBox startPicker;
    public final DateComboBox finishPicker;

    /**
     * Constructs panel with two date pickers for date range selection.
     * 
     * @param rangeStart
     * @param rangeFinish
     * @param rangeLowerBound
     * @param rangeUpperBound
     */
    public DateRange(final DateTime rangeStart, final DateTime rangeFinish, final DateTime rangeLowerBound, final DateTime rangeUpperBound) {
	final DefaultDateModel rangeStartModel = new DefaultDateModel();
	startPicker = newStartPicker(rangeStart, rangeFinish, rangeLowerBound, rangeStartModel);

	final DefaultDateModel rangeFinishModel = new DefaultDateModel();
	finishPicker = newFinishPicker(rangeStart, rangeFinish, rangeUpperBound, rangeFinishModel);

	// while changing the value of range start the lower bound in range finish date picker is adjusted accordingly
	startPicker.addItemListener(new ItemListener() {
	    public void itemStateChanged(final ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() != null) {
		    rangeFinishModel.setMinDate((Calendar) e.getItem());
		}
	    }
	});

	// while changing the value of range finish the upper bound in range start date picker is adjusted accordingly
	finishPicker.addItemListener(new ItemListener() {
	    public void itemStateChanged(final ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() != null) {
		    rangeStartModel.setMaxDate((Calendar) e.getItem());
		}
	    }
	});
    }

    /**
     * Instantiates a new start date picker.
     * 
     * @param rangeStart
     * @param rangeFinish
     * @param rangeLowerBound
     * @param rangeStartModel
     * @return
     */
    private DateComboBox newStartPicker(final DateTime rangeStart, final DateTime rangeFinish, final DateTime rangeLowerBound, final DefaultDateModel rangeStartModel) {
	final DateComboBox startPicker = new DateComboBox(rangeStartModel);
	startPicker.setShowNoneButton(false);
	startPicker.setShowTodayButton(false);
	rangeStartModel.setMinDate(rangeLowerBound.toCalendar(Locale.getDefault()));
	rangeStartModel.setMaxDate(rangeFinish.toCalendar(Locale.getDefault()));
	startPicker.setPrototypeDisplayValue(new DateTime().toCalendar(Locale.getDefault()));
	startPicker.setDate(rangeStart.toDate());
	return startPicker;
    }

    /**
     * Instantiates a new finish date picker.
     * 
     * @param rangeStart
     * @param rangeFinish
     * @param rangeUpperBound
     * @param rangeFinishModel
     * @return
     */
    private DateComboBox newFinishPicker(final DateTime rangeStart, final DateTime rangeFinish, final DateTime rangeUpperBound, final DefaultDateModel rangeFinishModel) {
	final DateComboBox finishPicker = new DateComboBox(rangeFinishModel);
	finishPicker.setShowNoneButton(false);
	finishPicker.setShowTodayButton(false);
	rangeFinishModel.setMinDate(rangeStart.toCalendar(Locale.getDefault()));
	rangeFinishModel.setMaxDate(rangeUpperBound.toCalendar(Locale.getDefault()));
	finishPicker.setPrototypeDisplayValue(new DateTime().toCalendar(Locale.getDefault()));
	finishPicker.setDate(rangeFinish.toDate());
	return finishPicker;
    }

    /**
     * Returns range start
     * 
     * @return
     */
    public DateTime getRangeStart() {
	return new DateTime(startPicker.getDate());
    }

    /**
     * Returns range finish
     * 
     * @return
     */
    public DateTime getRangeFinish() {
	return new DateTime(finishPicker.getDate());
    }

    public static void main(final String[] args) {
	for (final LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
	    if ("Nimbus".equals(laf.getName())) {
		try {
		    UIManager.setLookAndFeel(laf.getClassName());
		} catch (final Exception e) {
		    e.printStackTrace();
		}
	    }
	}
	com.jidesoft.utils.Lm.verifyLicense("Fielden Management Services", "Rollingstock Management System", "xBMpKdqs3vWTvP9gxUR4jfXKGNz9uq52");
	LookAndFeelFactory.installJideExtension();

	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		final JFrame frame = new JFrame("Testing Date Range Panel");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setLayout(new MigLayout("fill"));
		final DateRange range = new DateRange(new DateTime(2008, 7, 1, 0, 0, 0, 0), new DateTime(2008, 7, 10, 0, 0, 0, 0), new DateTime(2008, 4, 27, 0, 0, 0, 0), new DateTime(2008, 8, 28, 0, 0, 0, 0));
		frame.add(range.startPicker, "growx, wrap");
		frame.add(range.finishPicker, "growx, wrap");
		frame.pack();
		RefineryUtilities.centerFrameOnScreen(frame);
		frame.setVisible(true);
	    }
	});
    }
}