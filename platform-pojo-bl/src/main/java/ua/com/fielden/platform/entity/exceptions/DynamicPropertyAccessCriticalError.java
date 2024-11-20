package ua.com.fielden.platform.entity.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * Indicates an occurrence of a critical error related to dynamic property access.
 * An error of this type indicates that dynamic property access must not be used to recover from the error.
 */
public final class DynamicPropertyAccessCriticalError extends AbstractPlatformRuntimeException {

    public DynamicPropertyAccessCriticalError(final String s) {
        super(s);
    }

    public DynamicPropertyAccessCriticalError(final String message, final Throwable cause) {
        super(message, cause);
    }

}
