package ua.com.fielden.platform.serialisation.jackson.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * A runtime exception to indicate any errors pertaining to deserialisation.
 * 
 * @author TG Team
 */
public class DeserialisationException extends AbstractPlatformRuntimeException {

    private static final long serialVersionUID = 1L;

    public DeserialisationException(final String msg) {
        super(msg);
    }

    public DeserialisationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
