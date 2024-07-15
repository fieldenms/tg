package ua.com.fielden.platform.serialisation.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * A runtime exception to indicate TG specific serialisation related exception.
 * 
 * @author TG Team
 *
 */
public class SerialisationException extends AbstractPlatformRuntimeException {

    private static final long serialVersionUID = 1L;
    
    public SerialisationException(final String msg) {
        super(msg);
    }
    
    public SerialisationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}