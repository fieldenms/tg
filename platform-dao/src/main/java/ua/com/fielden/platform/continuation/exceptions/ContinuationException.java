package ua.com.fielden.platform.continuation.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/// A runtime exception that indicates an invalid state or misuse related to a continuation.
///
public class ContinuationException extends AbstractPlatformRuntimeException {

    public ContinuationException(final String msg) {
        super(msg);
    }

    public ContinuationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}