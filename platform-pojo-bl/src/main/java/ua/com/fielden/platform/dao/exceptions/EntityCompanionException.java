package ua.com.fielden.platform.dao.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * Runtime exception that should be thrown from within entity companion implementation.
 * 
 * @author TG Team
 *
 */
public class EntityCompanionException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public EntityCompanionException(final String msg) {
        super(msg);
    }

    public EntityCompanionException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}