package ua.com.fielden.platform.web.centre.exceptions;

/**
 * Custom exception indicating custom property definition problems.
 *
 * @author TG Team
 *
 */
public class PropertyDefinitionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PropertyDefinitionException(final String msg) {
        super(msg);
    }

    public PropertyDefinitionException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
