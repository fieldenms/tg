package ua.com.fielden.platform.example.swing.treemenu;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.jfree.ui.RefineryUtilities;

import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressPane;
import ua.com.fielden.platform.swing.view.BaseFrame;

public class TreeMenuDemo extends AbstractUiApplication {

    @Override
    protected void beforeUiExposure(final String[] args, final SplashController splashController) throws Exception {
	for (final LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
	    if ("Nimbus".equals(laf.getName())) {
		try {
		    UIManager.setLookAndFeel(laf.getClassName());
		} catch (final Exception e) {
		    e.printStackTrace();
		}
	    }
	}
    }

    @Override
    protected void exposeUi(final String[] args, final SplashController splashController) throws Exception {

	SwingUtilities.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		for (final LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
		    if ("Nimbus".equals(laf.getName())) {
			try {
			    UIManager.setLookAndFeel(laf.getClassName());
			} catch (final Exception e) {
			    e.printStackTrace();
			}
		    }
		}

		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);

		final JFrame frame = new BaseFrame("Tree Spike");
		final BlockingIndefiniteProgressPane blockingPane = new BlockingIndefiniteProgressPane("", frame.getRootPane(), 2.5d, 18);
		frame.add(new TreeMenuDemoPanel(blockingPane));

		frame.pack();
		RefineryUtilities.centerFrameOnScreen(frame);
		frame.setVisible(true);
	    }

	});
    }

    public static void main(final String[] args) {
	new TreeMenuDemo().launch(args);
    }

}
