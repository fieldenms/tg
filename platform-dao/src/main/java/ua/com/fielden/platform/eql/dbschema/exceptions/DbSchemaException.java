package ua.com.fielden.platform.eql.dbschema.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * A runtime exception to indicate TG specific DB schema related exception.
 * 
 * @author TG Team
 *
 */
public class DbSchemaException extends AbstractPlatformRuntimeException {

    private static final long serialVersionUID = 1L;

    public DbSchemaException(final String msg) {
        super(msg);
    }

    public DbSchemaException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}