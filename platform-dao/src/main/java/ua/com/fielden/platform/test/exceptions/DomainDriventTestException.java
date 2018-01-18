package ua.com.fielden.platform.test.exceptions;

/**
 * A runtime exception that indicates incorrect or unexpected behaviour during a test case construction.
 * 
 * @author TG Team
 *
 */
public class DomainDriventTestException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public DomainDriventTestException(final String msg) {
        super(msg);
    }
    
    public DomainDriventTestException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
