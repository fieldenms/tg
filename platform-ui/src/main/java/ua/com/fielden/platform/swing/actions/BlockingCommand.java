package ua.com.fielden.platform.swing.actions;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jfree.ui.RefineryUtilities;

import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressPane;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;

/**
 * This is a {@link Command} descendant, which provides blocking pane capability. Methods <code>preAction</code> and <code>postAction</code> were overridden to to start and stop
 * blocking respectively.
 * 
 * Method {@link BlockingCommand#setMessage(String)} can be used for updating the message displayed by blocking pane in the custom <code>action</code> implementation is requried
 * 
 * @author 01es
 * 
 * @param <T>
 */
public abstract class BlockingCommand<T> extends Command<T> {
    private static final long serialVersionUID = 1L;

    private final BlockingIndefiniteProgressPane pane;

    public BlockingCommand(final String name, final BlockingIndefiniteProgressPane pane) {
	super(name);
	this.pane = pane;
    }

    /**
     * Enhanced the notion of enable/disable by taking into account blocking pane locking.
     * 
     * @param enable
     *            -- if <code>true</code> then action is enabled and blocking pane unlocked; otherwise -- action is disabled and blocking pane locked.
     */
    @Override
    public void setEnabled(final boolean enable) {
	super.setEnabled(enable);
	if (enable) {
	    pane.unlock();
	} else {
	    pane.lock();
	}
    }

    /**
     * Method {@link #setEnabled(boolean)} handles both action enabling/disabling and blocking pane locking/unlocking where action disabling happens together with locking, and
     * enabling with unlocking.
     * <p>
     * However, this usage pattern is not always applicable.
     * <p>
     * In order to facilitate asymmetric enabling/locking this method has been proved, which accepts two parameters.
     * 
     * @param enable
     *            -- controls action enabling/disabling.
     * @param lock
     *            -- controls blocking pane locking/unlocking.
     */
    public void setEnabled(final boolean enable, final boolean lock) {
	super.setEnabled(enable);
	if (lock) {
	    pane.lock();
	} else {
	    pane.unlock();
	}

    }

    /**
     * Updates blocking pane with provided message.
     * 
     * @param value
     */
    public void setMessage(final String value) {
	SwingUtilitiesEx.invokeLater(new Runnable() {
	    public void run() {
		pane.setText(value);
	    }
	});
    }

    public static void main(final String[] args) {
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		final JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(600, 400));
		final BlockingIndefiniteProgressPane blockingPane = new BlockingIndefiniteProgressPane("blocking...", frame.getRootPane());
		frame.add(new JPanel(), BorderLayout.CENTER);
		final JPanel panel = new JPanel();
		final JButton button = new JButton(new BlockingCommand<String>("Start blocking", blockingPane) {
		    private static final long serialVersionUID = 1L;

		    @Override
		    protected String action(final ActionEvent e) throws Exception {
			setMessage("some message...");
			Thread.sleep(1000);
			setMessage("message...");
			Thread.sleep(1000);
			return "Finished";
		    }
		});
		panel.add(button);
		frame.add(panel, BorderLayout.SOUTH);
		frame.pack();
		RefineryUtilities.centerFrameOnScreen(frame);
		frame.setVisible(true);
	    }
	});
    }
}
