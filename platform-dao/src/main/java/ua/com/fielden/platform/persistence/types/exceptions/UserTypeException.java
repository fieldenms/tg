package ua.com.fielden.platform.persistence.types.exceptions;

/**
 * A runtime exception that indicates erroneous situation pertaining to the use of Hibernate user types. 
 * 
 * @author TG Team
 *
 */
public class UserTypeException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public UserTypeException(final String msg) {
        super(msg);
    }
    
    public UserTypeException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
