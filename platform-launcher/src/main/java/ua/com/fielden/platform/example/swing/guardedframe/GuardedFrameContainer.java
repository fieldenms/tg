/**
 *
 */
package ua.com.fielden.platform.example.swing.guardedframe;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.jfree.ui.RefineryUtilities;

import ua.com.fielden.platform.swing.model.ICloseGuard;
import ua.com.fielden.platform.swing.view.BaseFrame;
import ua.com.fielden.platform.swing.view.BasePanel;

/**
 * Frame for demonstration purposes
 * 
 * @author Yura
 */
public class GuardedFrameContainer extends JFrame {

    private static final long serialVersionUID = -2021597289801852282L;

    private final BaseFrame frame;

    @SuppressWarnings("serial")
    public GuardedFrameContainer() {
	setLayout(new MigLayout("fill", "[:80:][grow,fill]", "[][grow, fill]"));
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	frame = new BaseFrame("Guarded Frame");
	frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);// to stop frame from being closed uncontrollably
	frame.setPreferredSize(new Dimension(200, 200));

	final BasePanel guardedPanel = new BasePanel() {
	    @Override
	    public String getInfo() {
		return "No info";
	    }
	};
	guardedPanel.add(new JButton(new AbstractAction("Close") {
	    @Override
	    public void actionPerformed(final ActionEvent e) {
		final ICloseGuard guard = frame.canClose();
		if (guard == null) {
		    frame.close();
		} else {
		    notify(guard);
		}
	    }

	    private void notify(final ICloseGuard guard) {
		JOptionPane.showMessageDialog(frame, guard.whyCannotClose(), "Warning", JOptionPane.WARNING_MESSAGE);
	    }
	}));

	final JPanel simplePanel = new JPanel();
	simplePanel.add(new BasePanel() {

	    @Override
	    public ICloseGuard canClose() {
		return this;
	    }

	    @Override
	    public String whyCannotClose() {
		return "cannot close guarded frame";
	    }

	    @Override
	    public String getInfo() {
		return "No info";
	    }
	});

	guardedPanel.add(simplePanel);
	frame.add(guardedPanel);
	frame.pack();
	RefineryUtilities.centerFrameOnScreen(frame);

	final Action action = new AbstractAction("Open Frame") {
	    @Override
	    public void actionPerformed(final ActionEvent e) {
		frame.setVisible(true);
	    }
	};
	add(new JButton(action));
	pack();
    }

    public JFrame getFrame() {
	return frame;
    }

}
