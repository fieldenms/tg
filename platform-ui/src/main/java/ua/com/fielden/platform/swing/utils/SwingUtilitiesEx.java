package ua.com.fielden.platform.swing.utils;

import static javax.swing.UIManager.getInstalledLookAndFeels;
import static javax.swing.UIManager.setLookAndFeel;

import java.awt.EventQueue;

import javax.swing.SwingUtilities;
import javax.swing.UIManager.LookAndFeelInfo;

public class SwingUtilitiesEx {

    /**
     * This method will execute passed {@link Runnable} right now, if it is event-dispatch thread or will call {@link EventQueue#invokeLater(Runnable)} with passed {@link Runnable}
     * instance otherwise.
     * 
     * @param runnable
     */
    public static void invokeLater(final Runnable runnable) {
	if (SwingUtilities.isEventDispatchThread()) {
	    runnable.run();
	} else {
	    EventQueue.invokeLater(runnable);
	}
    }

    /**
     * The implementation of {@link EventQueue#invokeAndWait(Runnable)} throws an exception if the method is invoked on EDT. This method calls
     * {@link EventQueue#invokeAndWait(Runnable)} if it is invoked not on EDT. Otherwise, {@link Runnable#run()} is called directly.
     * 
     * @param runnable
     * @throws Exception
     */
    public static void invokeAndWaitIfPossible(final Runnable runnable) throws Exception {
	if (SwingUtilities.isEventDispatchThread()) {
	    runnable.run();
	} else {
	    EventQueue.invokeAndWait(runnable);
	}
    }

    public static void installNimbusLnFifPossible() {
	for (final LookAndFeelInfo laf : getInstalledLookAndFeels()) {
	    if ("Nimbus".equals(laf.getName())) {
		try {
		    setLookAndFeel(laf.getClassName());
		} catch (final Exception e) {
		    e.printStackTrace();
		}
	    }
	}
    }

}
