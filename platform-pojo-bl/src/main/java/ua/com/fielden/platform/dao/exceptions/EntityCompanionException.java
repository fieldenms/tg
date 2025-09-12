package ua.com.fielden.platform.dao.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/// Runtime exception that should be thrown from within entity companion implementation.
///
public class EntityCompanionException extends AbstractPlatformRuntimeException {

    public EntityCompanionException(final String msg) {
        super(msg);
    }

    public EntityCompanionException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}