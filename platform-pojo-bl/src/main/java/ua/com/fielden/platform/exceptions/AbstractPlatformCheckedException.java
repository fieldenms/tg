package ua.com.fielden.platform.exceptions;

/**
 * A checked exception type, which should be used as a base class for all checked platform-level exceptions, indicating some technical (rather than domain related) invalid situation.
 *
 * @author TG Team
 *
 */
public abstract class AbstractPlatformCheckedException extends Exception {
    private static final long serialVersionUID = 1L;

    public AbstractPlatformCheckedException(final String msg) {
        super(msg);
    }

    public AbstractPlatformCheckedException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}