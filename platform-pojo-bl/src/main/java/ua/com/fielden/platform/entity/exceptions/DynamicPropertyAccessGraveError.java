package ua.com.fielden.platform.entity.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * Indicates an occurence of a grave error related to dynamic property access. An error of this type indicates that
 * dynamic property access must not be used to recover from the error.
 */
public final class DynamicPropertyAccessGraveError extends AbstractPlatformRuntimeException {

    public DynamicPropertyAccessGraveError(final String s) {
        super(s);
    }

    public DynamicPropertyAccessGraveError(final String message, final Throwable cause) {
        super(message, cause);
    }

}
