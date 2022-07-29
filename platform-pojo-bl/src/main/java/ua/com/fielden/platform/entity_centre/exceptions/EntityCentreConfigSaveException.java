package ua.com.fielden.platform.entity_centre.exceptions;
/**
 * A runtime exception that indicates incorrect situation that pertain to execution of entity centres. 
 * This exception, for example, could be used to represent an error during dynamic generation of EQL from entity centres.
 * 
 * @author TG Team
 *
 */
public class EntityCentreConfigSaveException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public EntityCentreConfigSaveException(final String msg) {
        super(msg);
    }
    
    public EntityCentreConfigSaveException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
