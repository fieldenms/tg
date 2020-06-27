package ua.com.fielden.platform.entity.exceptions;

/**
 * A runtime exception to indicate incorrect definitions of BCE handlers as part of annotation {@code @BeforeChange}.
 * 
 * @author TG Team
 *
 */
public class PropertyBceDefinitionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PropertyBceDefinitionException(final String msg) {
        super(msg);
    }

    public PropertyBceDefinitionException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
