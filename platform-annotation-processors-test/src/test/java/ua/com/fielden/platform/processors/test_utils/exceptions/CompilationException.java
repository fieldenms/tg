package ua.com.fielden.platform.processors.test_utils.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * A generic exception to indicate errors that happened during compilation, invoked in unit tests. 
 *
 * @author TG Team
 *
 */
public class CompilationException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public CompilationException(final String message) {
        super(message);
    }

    public CompilationException(final Throwable cause) {
        super(cause);
    }

    public CompilationException(final String message, final Throwable cause) {
        super(message, cause);
    }

}