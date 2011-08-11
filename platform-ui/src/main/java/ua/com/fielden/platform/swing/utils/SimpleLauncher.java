package ua.com.fielden.platform.swing.utils;

import java.awt.LayoutManager;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;

import org.jfree.ui.RefineryUtilities;

import ua.com.fielden.platform.swing.view.BaseFrame;
import ua.com.fielden.platform.swing.view.ICloseHook;

/**
 * This is a convenience class for visual UI testing.
 * 
 * @author 01es
 * 
 */
public class SimpleLauncher {
    public static void show(final String caption, final LayoutManager manager, final JComponent... list) {
	SwingUtilitiesEx.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		SwingUtilitiesEx.installNimbusLnFifPossible();

		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);

		final JFrame frame = new BaseFrame(caption, new ICloseHook<BaseFrame>() {
		    @Override
		    public void closed(final BaseFrame frame) {
			System.exit(0);
		    }
		});
		if (manager != null) {
		    frame.setLayout(manager);
		}
		for (final JComponent component : list) {
		    frame.add(component);
		}
		frame.pack();
		RefineryUtilities.centerFrameOnScreen(frame);
		frame.setVisible(true);
	    }

	});
    }

    public static void show(final String caption, final JFrame jframe, final LayoutManager manager, final JComponent... list) {
	SwingUtilitiesEx.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		SwingUtilitiesEx.installNimbusLnFifPossible();

		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);

		if (jframe != null) {
		    jframe.setTitle(caption);
		}
		final JFrame frame = jframe != null ? jframe : new BaseFrame(caption);
		if (manager != null) {
		    frame.setLayout(manager);
		}
		for (final JComponent component : list) {
		    frame.add(component);
		}
		frame.pack();
		RefineryUtilities.centerFrameOnScreen(frame);
		frame.setVisible(true);
	    }

	});
    }

    public static void show(final String caption, final JComponent... list) {
	show(caption, null, list);
    }
}
