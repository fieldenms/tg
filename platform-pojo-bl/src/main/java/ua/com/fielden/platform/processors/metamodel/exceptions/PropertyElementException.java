package ua.com.fielden.platform.processors.metamodel.exceptions;

/**
 * An exception time to report error pertaining to domain meta-model generation.
 *
 * @author TG Team
 *
 */
public class PropertyElementException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PropertyElementException(final String message) {
        super(message);
    }

}