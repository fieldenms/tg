package ua.com.fielden.platform.types.exceptions;

/**
 * A runtime exception that indicates erroneous situation pertaining to validation or instantiation of value objects. 
 * 
 * @author TG Team
 *
 */
public class ValueObjectException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public ValueObjectException(final String msg) {
        super(msg);
    }
    
    public ValueObjectException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
