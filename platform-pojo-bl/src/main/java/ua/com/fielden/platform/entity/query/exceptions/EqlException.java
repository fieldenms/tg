package ua.com.fielden.platform.entity.query.exceptions;

/**
 * A generic runtime exception indicating any EQL related erroneous situation.
 * 
 * @author TG Team
 *
 */
public class EqlException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EqlException(final String msg) {
        super(msg);
    }
    
    public EqlException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
    
}
