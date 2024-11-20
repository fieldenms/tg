package ua.com.fielden.platform.reflection.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * A runtime exception to indicate TG specific reflection related exception.
 * 
 * @author TG Team
 *
 */
public class ReflectionException extends AbstractPlatformRuntimeException {

    private static final long serialVersionUID = 1L;
    
    public ReflectionException(final String msg) {
        super(msg);
    }

    public ReflectionException(final Throwable cause) {
        super(cause);
    }

    public ReflectionException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}