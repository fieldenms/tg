package ua.com.fielden.platform.exceptions;

/// A checked exception type, which should be used as a base class for all checked platform-level exceptions, indicating some technical (rather than domain related) invalid situation.
///
public abstract class AbstractPlatformCheckedException extends Exception {

    public AbstractPlatformCheckedException(final String msg) {
        super(msg);
    }

    public AbstractPlatformCheckedException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}