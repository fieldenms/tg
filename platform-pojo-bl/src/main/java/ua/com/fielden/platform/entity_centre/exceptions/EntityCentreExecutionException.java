package ua.com.fielden.platform.entity_centre.exceptions;

/**
 * A runtime exception that indicates incorrect situation pertaining to a failure to save an Entity Centre configuration. 
 *
 * @author TG Team
 *
 */
public class EntityCentreExecutionException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    public EntityCentreExecutionException(final String msg) {
        super(msg);
    }
    
    public EntityCentreExecutionException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}