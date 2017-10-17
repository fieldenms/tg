package ua.com.fielden.platform.web.action.exceptions;

/**
 * A runtime exception that indicates erroneous situation pertaining to an action configuration.
 *
 * @author TG Team
 *
 */
public class ActionConfigurationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ActionConfigurationException(final String msg) {
        super(msg);
    }

    public ActionConfigurationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
