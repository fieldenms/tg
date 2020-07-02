package ua.com.fielden.platform.entity.exceptions;

/**
 * A runtime exception to indicate incorrect definitions of BCE or ACE handlers as part of annotation {@code @BeforeChange} and {@code @AfterChange}.
 * 
 * @author TG Team
 *
 */
public class PropertyBceOrAceDefinitionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PropertyBceOrAceDefinitionException(final String msg) {
        super(msg);
    }

    public PropertyBceOrAceDefinitionException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
