package ua.com.fielden.platform.processors.metamodel.exceptions;

/**
 * An exception to report unexpected situations due to some unpredictable changes to classes during annotation processing.
 * 
 * @author TG Team
 *
 */
public class UnexpectedStateException extends MetaModelProcessorException {
    private static final long serialVersionUID = 1L;

    public UnexpectedStateException(final String message) {
        super(message);
    }

}