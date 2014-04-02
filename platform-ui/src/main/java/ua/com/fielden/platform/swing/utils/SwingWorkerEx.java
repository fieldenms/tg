package ua.com.fielden.platform.swing.utils;

import javax.swing.SwingWorker;

/**
 * This is a descendant of {@link SwingWorker}, which implements exception handling for the background action supporting custom logic to handle exceptions.
 * <ul>
 * <li>Method {@link #backgroundAction()} should be overridden to provide the logic to be executed on the background.
 * <li>Method {@link #handleException(Throwable)} should be implemented to provide custom logic for exception handling.
 * </ul>
 * 
 * @author 01es
 * 
 */
public abstract class SwingWorkerEx<T, V> extends SwingWorker<T, V> implements Thread.UncaughtExceptionHandler {

    /**
     * Method that should be implemented in place of usually overridden {@link #doInBackground()}.
     * 
     * @return
     */
    protected abstract T backgroundAction() throws Exception;

    /**
     * Method for implementing a custom exception handling that could happen during the background action.
     * 
     * @param ex
     * @return
     */
    protected abstract void handleException(final Throwable ex);

    /**
     * Provides exception wrapping. Should never be overridden -- thus final. Method {@link #backgroundAction()} should be used for implementing the logic to be executed on the
     * background.
     */
    @Override
    protected final T doInBackground() throws Exception {
        // for some reason this does not work: Thread.currentThread().setUncaughtExceptionHandler(this);
        try {
            return backgroundAction();
        } catch (final Throwable ex) {
            handleException(ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * The method implemented as part of interface {@link Thread.UncaughtExceptionHandler} -- it simply invokes method {@link #handleException(Throwable)}.
     */
    @Override
    public final void uncaughtException(final Thread t, final Throwable e) {
        handleException(e);
    }
}
