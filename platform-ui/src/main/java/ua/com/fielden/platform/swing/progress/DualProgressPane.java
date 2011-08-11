package ua.com.fielden.platform.swing.progress;

import java.awt.event.ActionEvent;
import java.util.Random;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;

/**
 * A pane with two progress bars -- one indicating overall progress, another -- the progress of the currently running task.
 *
 * @author TG Team
 *
 */
public class DualProgressPane extends JPanel {
    private static final long serialVersionUID = 1L;

    private final JLabel label;
    private final JProgressBar pbCurrent;
    private final JProgressBar pbOverall;

    /**
     * Constructs an instance with maxOverallSteps indicating the total steps to be performed by the process using the progress pane.
     *
     * @param maxOverallSteps
     *            -- the total number of steps
     */
    public DualProgressPane(final int maxOverallSteps) {
	setLayout(new MigLayout("fill, insets 0", "[fill, grow, :300:]", "[fill][fill, grow][fill, grow]"));

	add(label = new JLabel("Message"), "wrap, wmin 100");
	pbCurrent = new JProgressBar();
	pbCurrent.setStringPainted(true);
	pbCurrent.setToolTipText("Indicates a progress of the current action");
	add(pbCurrent, "wrap");
	pbOverall = new JProgressBar(0, maxOverallSteps);
	pbOverall.setStringPainted(true);
	pbOverall.setToolTipText("Indicates an overall progress");
	add(pbOverall);

    }

    /**
     * Initiates the progress bar responsible for providing a feedback for the currently running task.
     *
     * @param maxCurrSteps
     * @param msg
     * @throws Exception
     */
    public void initNewStep(final int maxCurrSteps, final String msg) throws Exception {
	SwingUtilitiesEx.invokeAndWaitIfPossible(new Runnable() {
	    @Override
	    public void run() {
		label.setText(msg);
		pbCurrent.setModel(new DefaultBoundedRangeModel(0, 0, 0, maxCurrSteps));
	    }
	});
    }

    /**
     * Completes indication for the currently running task by updating the overall progress.
     *
     * @param msg
     * @throws Exception
     */
    public void finishCurrStep(final String msg) throws Exception {
	SwingUtilitiesEx.invokeAndWaitIfPossible(new Runnable() {
	    @Override
	    public void run() {
		label.setText(msg);
		pbCurrent.setValue(pbCurrent.getMaximum());
		pbOverall.setValue(pbOverall.getValue() + 1);
	    }
	});
    }

    /**
     * Updates the progress bar for the currently running task with the provided progress.
     *
     * @param extent
     * @throws Exception
     */
    public void updateCurrStep(final int extent) throws Exception {
	SwingUtilitiesEx.invokeAndWaitIfPossible(new Runnable() {
	    @Override
	    public void run() {
		pbCurrent.setValue(extent);
	    }
	});
    }

    /**
     * Sets the progress pane into the initial state.
     *
     * @param msg
     * @throws Exception
     */
    public void reset(final String msg) throws Exception {
	SwingUtilitiesEx.invokeAndWaitIfPossible(new Runnable() {
	    @Override
	    public void run() {
		label.setText(msg);
		pbOverall.setValue(0);
		pbCurrent.setValue(0);
	    }
	});
    }

    /**
     * Sets the progress pane into the completed state.
     *
     * @param msg
     * @throws Exception
     */
    public void completeAll(final String msg) throws Exception {
	SwingUtilitiesEx.invokeAndWaitIfPossible(new Runnable() {
	    @Override
	    public void run() {
		label.setText(msg);
		pbOverall.setModel(new DefaultBoundedRangeModel(1, 0, 0, 1));
		pbCurrent.setModel(new DefaultBoundedRangeModel(1, 0, 0, 1));
	    }
	});
    }

    /**
     * Can be used to update a message in the label without effecting any of the progress bars.
     *
     * @param msg
     * @throws Exception
     */
    public void message(final String msg) throws Exception {
	SwingUtilitiesEx.invokeAndWaitIfPossible(new Runnable() {
	    @Override
	    public void run() {
		label.setText(msg);
	    }
	});
    }

    public static void main(final String[] args) {
	final DualProgressPane pane = new DualProgressPane(10);

	final Command<Void> command = new Command<Void>("Run") {

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		pane.reset("Initialising");
		final Random rnd = new Random();
		for (int step = 0; step < 10; step++) {
		    final int maxCurrSteps = rnd.nextInt(10) + 1;
		    pane.initNewStep(maxCurrSteps, "<html>Performing step " + (step + 1) + " of " + 10 + "</html>");
		    int currProgress = 0;
		    for (int currStep = 0; currStep < maxCurrSteps; currStep++) {
			Thread.sleep(rnd.nextInt(500));
			pane.updateCurrStep(++currProgress);
		    }
		    pane.finishCurrStep("Step " + step + " is completed");
		}
		pane.completeAll("<html><font color=#00AA00>Completed</font></html>");
		return null;
	    }

	};

	final Command<Void> commandWithException = new Command<Void>("Run with Error") {

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		pane.reset("Initialising");
		try {
		    final Random rnd = new Random();
		    final int maxCurrSteps = rnd.nextInt(10);
		    pane.initNewStep(maxCurrSteps, "<html>Performing step 1 of 10</html>");
		    int currProgress = 0;
		    for (int currStep = 0; currStep < maxCurrSteps - 1; currStep++) {
			Thread.sleep(rnd.nextInt(500));
			pane.updateCurrStep(++currProgress);
		    }
		    throw new Exception("Could not download the resource");
		} catch (final Exception ex) {
		    pane.message("<html><font color=#AA0000>Error: " + ex.getMessage() + "</font></html>");
		}
		return null;
	    }

	};

	final JPanel panel = new JPanel(new MigLayout("fill", "[fill, grow][fill, grow]", "[fill, grow][]"));
	panel.add(pane, "span");
	panel.add(new JButton(command));
	panel.add(new JButton(commandWithException));

	SimpleLauncher.show("Progress pane example", panel);

    }
}
