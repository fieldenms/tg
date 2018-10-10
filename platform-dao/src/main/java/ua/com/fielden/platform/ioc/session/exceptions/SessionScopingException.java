package ua.com.fielden.platform.ioc.session.exceptions;

/**
 * A runtime exception that indicates incorrect scoping of a database session/transaction.
 * 
 * @author TG Team
 *
 */
public class SessionScopingException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public SessionScopingException(final String msg) {
        super(msg);
    }
    
    public SessionScopingException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
