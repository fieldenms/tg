package ua.com.fielden.platform.entity.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * An exception that pertains to the property indexer functionality.
 */
public final class PropertyIndexerException extends AbstractPlatformRuntimeException {

    public PropertyIndexerException(final String s) {
        super(s);
    }

    public PropertyIndexerException(final Throwable cause) {
        super(cause);
    }

    public PropertyIndexerException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
