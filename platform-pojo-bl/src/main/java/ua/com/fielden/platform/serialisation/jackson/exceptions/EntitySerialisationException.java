package ua.com.fielden.platform.serialisation.jackson.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * A runtime exception to indicate any errors pertaining to entity serialisation.
 * 
 * @author TG Team
 *
 */
public class EntitySerialisationException extends AbstractPlatformRuntimeException {

    private static final long serialVersionUID = 1L;

    public EntitySerialisationException(final String msg) {
        super(msg);
    }
    
    public EntitySerialisationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}