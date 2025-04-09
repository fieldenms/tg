package ua.com.fielden.platform.entity.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

///
/// A runtime exception, which should be used to indicate an invalid state situation.
///
public class InvalidStateException extends AbstractPlatformRuntimeException {

    public InvalidStateException(final String msg) {
        super(msg);
    }

    public InvalidStateException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
