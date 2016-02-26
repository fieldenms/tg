package ua.com.fielden.platform.entity.exceptions;

/**
 * A runtime exception that indicates erroneous situation pertaining to entity instantiation or access. 
 * 
 * @author TG Team
 *
 */
public class EntityException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public EntityException(final String msg) {
        super(msg);
    }
    
    public EntityException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
