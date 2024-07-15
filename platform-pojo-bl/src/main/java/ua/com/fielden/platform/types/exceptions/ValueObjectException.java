package ua.com.fielden.platform.types.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * A runtime exception that indicates erroneous situation pertaining to validation or instantiation of value objects. 
 * 
 * @author TG Team
 *
 */
public class ValueObjectException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;
    
    public ValueObjectException(final String msg) {
        super(msg);
    }
    
    public ValueObjectException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
