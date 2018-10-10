package ua.com.fielden.platform.utils;

/**
 * Runtime exception that should be thrown from within {@link DefinersExecutor} implementation.
 * 
 * @author TG Team
 *
 */
public class DefinersExecutorException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DefinersExecutorException(final String msg) {
        super(msg);
    }
    
    public DefinersExecutorException(final String msg, final Exception cause) {
        super(msg, cause);
    }
}
