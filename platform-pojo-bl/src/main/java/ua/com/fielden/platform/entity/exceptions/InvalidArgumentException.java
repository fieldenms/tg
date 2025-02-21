package ua.com.fielden.platform.entity.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * A runtime exception, which should be used to indicate invalid method arguments. 
 * 
 * @author TG Team
 *
 */
public class InvalidArgumentException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * A convenient factory method that either create a new instance of {@code InvalidArgumentException} or returns {@code cause} if it is already of this type.
     * 
     * @param msg
     * @param cause
     * @return
     */
    public static InvalidArgumentException wrapIfNecessary(final String msg, final Exception cause) {
        return cause instanceof InvalidArgumentException ? (InvalidArgumentException) cause : new InvalidArgumentException(msg, cause);
    }

    public static <X> X requireNonNull(final X argument, final String name) {
        if (argument == null) {
            throw new InvalidArgumentException("Argument [%s] must not be null.".formatted(name));
        }
        return argument;
    }

    public static void requireNull(final Object argument, final String name) {
        if (argument != null) {
            throw new InvalidArgumentException("Argument [%s] must be null.".formatted(name));
        }
    }

    public InvalidArgumentException(final String msg) {
        super(msg);
    }

    public InvalidArgumentException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
