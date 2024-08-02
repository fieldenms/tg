package ua.com.fielden.platform.serialisation.jackson.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * A runtime exception to indicate any errors pertaining to entity deserialisation.
 * 
 * @author TG Team
 *
 */
public class EntityDeserialisationException extends DeserialisationException {

    private static final long serialVersionUID = 1L;

    public EntityDeserialisationException(final String msg) {
        super(msg);
    }
    
    public EntityDeserialisationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
