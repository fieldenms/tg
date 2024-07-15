package ua.com.fielden.platform.web.action.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * A runtime exception that indicates erroneous situation pertaining to an action configuration.
 *
 * @author TG Team
 *
 */
public class ActionConfigurationException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public ActionConfigurationException(final String msg) {
        super(msg);
    }

    public ActionConfigurationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}