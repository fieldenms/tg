package ua.com.fielden.platform.entity.exceptions;

/**
 * A runtime exception that indicates erroneous situation pertaining to entity instantiation or access. 
 * 
 * @author TG Team
 *
 */
public class EntityException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * A convenient factory method that either create a new instance of {@code EntityException} or returns {@code cause} if it is already of this type.
     * 
     * @param msg
     * @param cause
     * @return
     */
    public static EntityException wrapIfNecessary(final String msg, final Exception cause) {
        return cause instanceof EntityException ? (EntityException) cause : new EntityException(msg, cause);
    }

    public EntityException(final String msg) {
        super(msg);
    }
    
    public EntityException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
