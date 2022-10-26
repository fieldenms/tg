package ua.com.fielden.platform.entity_master.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * Runtime exception that should be thrown when illegal situations occur during simple master lifecycle.
 * 
 * @author TG Team
 *
 */
public class SimpleMasterException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public SimpleMasterException(final String msg) {
        super(msg);
    }

    public SimpleMasterException(final String msg, final Exception cause) {
        super(msg, cause);
    }

}