package ua.com.fielden.platform.processors.metamodel.exceptions;

/**
 * An exception time to report error pertaining to domain meta-model generation.
 *
 * @author TG Team
 *
 */
public class ForwardingElementException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ForwardingElementException(final String message) {
        super(message);
    }

}