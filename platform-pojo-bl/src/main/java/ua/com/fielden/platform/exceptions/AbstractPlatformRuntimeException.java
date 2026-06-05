package ua.com.fielden.platform.exceptions;

/// A runtime exception type, which should be used as a base class for all platform-level exceptions, indicating some technical (rather than domain related) invalid situation.
///
public abstract class AbstractPlatformRuntimeException extends RuntimeException {

    public AbstractPlatformRuntimeException(final String msg) {
        super(msg);
    }

    public AbstractPlatformRuntimeException(final Throwable cause) {
        super(cause);
    }

    public AbstractPlatformRuntimeException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}