/**
 *
 */
package ua.com.fielden.platform.application;

import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import ua.com.fielden.platform.branding.SplashController;

/**
 * General base class for all applications that have user interface.
 * 
 * @author TG Team
 */
public abstract class AbstractUiApplication extends AbstractApplication {

    public AbstractUiApplication() {
        super();
    }

    public AbstractUiApplication(final Logger logger) {
        super(logger);
    }

    /**
     * Sets system property "sun.awt.exception.handler" to {@link UncaughtExceptionHandler} class name. Also sets {@link UncaughtExceptionHandler#currentApplication} reference to
     * this application.
     * 
     * @see UncaughtExceptionHandler
     */
    private void setUncaughtExceptionHandler() {
        UncaughtExceptionHandler.setCurrentApplication(this);
        System.setProperty("sun.awt.exception.handler", UncaughtExceptionHandler.class.getName());
    }

    /**
     * Implementation in this class does absolutely nothing
     * 
     * @see #launch(String[])
     * @param args
     * @param splashController
     * @throws Exception
     */
    protected void beforeUiExposure(final String[] args, final SplashController splashController) throws Throwable {
    }

    /**
     * Invoked on EDT
     * 
     * @see #launch(String[])
     * @param args
     */
    protected abstract void exposeUi(String[] args, SplashController splashController) throws Throwable;

    /**
     * Implementation in this class does absolutely nothing
     * 
     * @see #launch(String[])
     * @param args
     * @param splashController
     * @throws Exception
     */
    protected void afterUiExposure(final String[] args, final SplashController splashController) throws Throwable {
    }

    /**
     * Launches application :
     * <ul>
     * <li>Creates instance of {@link SplashController}</li>
     * <li>Invokes {@link #beforeUiExposure(String[], SplashController)} method and if it throws {@link Exception}, this exception is redirected to
     * {@link #throwException(Exception)} method</li>
     * <li>Invokes {@link #exposeUi(String[], SplashController)} method on EDT and waits till it finish its invocation. Exception handling is the same as in the previous point</li>
     * <li>Invokes {@link #afterUiExposure(String[], SplashController)} method after {@link #exposeUi(String[], SplashController)} has finished its invocation. Exception handling
     * is the same as in the previous point</li>
     * </ul>
     * <br>
     * Note : invokes {@link #setUncaughtExceptionHandler()} method to set system property "sun.awt.exception.handler" at the very beginning of this method see JavaDocs for that
     * method for details
     */
    @Override
    public void launch(final String... args) {
        setUncaughtExceptionHandler();

        try {
            final SplashController splashController = new SplashController();
            beforeUiExposure(args, splashController);

            // waiting for EDT to finish exposing UI
            final Runnable exposeUiRunner = new Runnable() {
                @Override
                public void run() {
                    try {
                        exposeUi(args, splashController);
                    } catch (final Throwable e) {
                        throwException(e);
                    }
                }
            };
            if (SwingUtilities.isEventDispatchThread()) {
                exposeUiRunner.run();
            } else {
                try {
                    SwingUtilities.invokeLater(exposeUiRunner);
                } catch (final Exception e) {
                    throwException(e);
                }
            }

            afterUiExposure(args, splashController);
        } catch (final Throwable e) {
            throwException(e);
        }
    }

}
