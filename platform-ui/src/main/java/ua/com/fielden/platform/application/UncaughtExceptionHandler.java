/**
 *
 */
package ua.com.fielden.platform.application;

/**
 * Class for handling uncaught exceptions, thrown on EDT.<br>
 * <br>
 * Note : because this class is used via "sun.awt.exception.handler" system property, it is created inside EDT using no-argument constructor and thus it is not possible to get
 * instance of this handler which is used in EDT. Because of this we can only rely on static reference to our application, which should handle uncaught exception in its own way.
 * That is why private static field {@link #currentApplication} and package-private static setter for it were added in this class.<br>
 * <br>
 * TODO this logic is fragile and should be re-factored as soon as EDT supports normal customized handling of uncaught exceptions <br>
 * 
 * @see #setCurrentApplication(AbstractApplication)
 * 
 * @author Yura
 */
public class UncaughtExceptionHandler {

    private static AbstractApplication currentApplication;

    /**
     * Sets reference to current application. This method should be called from {@link AbstractUiApplication#launch(String[])} method at the beginning
     * 
     * @param application
     */
    static void setCurrentApplication(final AbstractApplication application) {
	currentApplication = application;
    }

    private static AbstractApplication getCurrentApplication() {
	return currentApplication;
    }

/**
     * Handles uncaught exception and invokes {@link AbstractUiApplication#throwException(Throwable)) method on #currentApplication instance with it
     * @param throwable
     */
    public void handle(final Throwable throwable) {
	getCurrentApplication().throwException(throwable);
    }

}
