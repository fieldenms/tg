package ua.com.fielden.platform.security.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * A runtime exception that indicates erroneous situation pertaining to user security. 
 * 
 * @author TG Team
 *
 */
public class SecurityException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;
    
    public SecurityException(final String msg) {
        super(msg);
    }
    
    public SecurityException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
