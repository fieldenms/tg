package ua.com.fielden.platform.entity.exceptions;

/**
 * A runtime exception that indicates incorrect entity definition.
 * 
 * @author TG Team
 *
 */
public class EntityDefinitionException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public EntityDefinitionException(final String msg) {
        super(msg);
    }
    
    public EntityDefinitionException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
