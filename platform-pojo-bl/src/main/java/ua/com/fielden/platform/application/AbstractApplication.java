/**
 *
 */
package ua.com.fielden.platform.application;

import java.util.logging.Logger;

/**
 * This class encapsulates only logging and exception throwing/handling functionality in order to provide support for business logic entities which may need to log something, or
 * throw some exceptions
 * 
 * @author TG Team
 */
public abstract class AbstractApplication {

    private final Logger logger;

    public AbstractApplication() {
	this.logger = Logger.getLogger(this.getClass().getName());
    }

    /**
     * Initialises application logger
     * 
     * @param logger
     */
    public AbstractApplication(final Logger logger) {
	this.logger = logger;
    }

    /**
     * Returns application logger
     * 
     * @return
     */
    public Logger getLogger() {
	return logger;
    }

    /**
     * This method should be used inside application to generate some critical {@link Throwable} (indicating <code>The Thing That Should Not Be</code>), that should be handled at
     * top level (i.e. application level). This method invokes {@link #handleException(Throwable)} with passed <code>exception</code> to handle it at the top level.
     * 
     * @param exception
     */
    public void throwException(final Throwable exception) {
	handleException(exception);
    }

    /**
     * Override this method to provide custom handling (showing dialog, logging exception, storing it to database) of top level exceptions (i.e. exceptions not supported by
     * business logic). Currently, this method simply prints stack trace of passed <code>exception</code>
     * 
     * @param exception
     */
    protected void handleException(final Throwable exception) {
	exception.printStackTrace();
    }

    public abstract void launch(String... args);

}
