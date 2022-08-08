package ua.com.fielden.platform.web.factories.webui.exceptions;

/**
 * An exception type that should be used to report unexpected exceptions during the logout resource construction or request processing.
 *
 * @author TG Team
 */
public class LogoutResourceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public LogoutResourceException(final String msg) {
        super(msg);
    }
    
    public LogoutResourceException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}