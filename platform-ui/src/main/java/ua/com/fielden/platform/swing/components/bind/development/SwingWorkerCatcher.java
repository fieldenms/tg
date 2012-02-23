package ua.com.fielden.platform.swing.components.bind.development;

import javax.swing.SwingWorker;

/**
 * This class is the improved descendant of the SwingWorker, that can catch and handle exceptions, that are thrown in the doInBackGround() method and done() method. use methods
 * tryToDoInBackground() and tryToDone() instead of DoInBackground() and Done(). you can also handle Exception by the special way, -> specify overridden implementation of the
 * handleException() method. Use this class, first of all, for binding Api in commit()-similar methods, when setting and validation need to be done on the separate thread.
 * 
 * @author jhou
 * 
 * @param <T>
 * @param <V>
 */
public abstract class SwingWorkerCatcher<T, V> extends SwingWorker<T, V> {

    /**
     * do not override it! use tryToDoInBackground()!
     */
    @Override
    protected final T doInBackground() {
	try {
	    return tryToDoInBackground();
	} catch (final Exception e) {
	    handleException(e);
	    return null;
	}
    }

    /**
     * do not override it! use tryToDone()!
     */
    @Override
    protected final void done() {
	try {
	    tryToDone();
	    super.done();
	} catch (final Exception e) {
	    handleException(e);
	}
    }

    /**
     * implement this method to do some logic on the separate thread and handle the exceptions throwed in that logic
     * 
     * @return
     */
    protected abstract T tryToDoInBackground() throws Exception;

    /**
     * override this method to do some "after-party" logic on EDT and handle the exceptions throwed in that logic
     */
    protected void tryToDone() {
	// default implementation - do nothing
    }

    /**
     * Override to get some special exception handling
     * 
     * @param e
     */
    protected void handleException(final Exception e) {
	e.printStackTrace();
    }
}
