package ua.com.fielden.platform.web.resources.webui.exceptions;

/**
 * Indicates problem in web ui client configuration process.
 *
 * @author TG Team
 *
 */
public class InvalidUiConfigException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidUiConfigException(final String msg) {
        super(msg);
    }

    public InvalidUiConfigException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}