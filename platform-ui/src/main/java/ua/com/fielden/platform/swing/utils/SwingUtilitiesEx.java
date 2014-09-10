package ua.com.fielden.platform.swing.utils;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

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
     */
    public static void invokeAndWaitIfPossible(final Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            try {
                EventQueue.invokeAndWait(runnable);
            } catch (InvocationTargetException | InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
    }

}
