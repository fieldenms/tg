package ua.com.fielden.platform.entity.query.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * A generic runtime exception indicating any EQL related erroneous situation.
 * 
 * @author TG Team
 *
 */
public class EqlException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public EqlException(final String msg) {
        super(msg);
    }

    public EqlException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
   
}