package ua.com.fielden.platform.serialisation.jackson.exceptions;

/**
 * A runtime exception to indicate any errors pertaining to entity serialisation.
 * 
 * @author TG Team
 *
 */
public class EntitySerialisationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EntitySerialisationException(final String msg) {
        super(msg);
    }
    
    public EntitySerialisationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
