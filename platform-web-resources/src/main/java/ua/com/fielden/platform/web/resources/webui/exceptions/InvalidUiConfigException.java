package ua.com.fielden.platform.web.resources.webui.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * Indicates a problem in Web UI client configuration.
 *
 * @author TG Team
 *
 */
public class InvalidUiConfigException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidUiConfigException(final String msg) {
        super(msg);
    }

    public InvalidUiConfigException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}