package ua.com.fielden.platform.entity.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/// A runtime exception, which should be used to indicate invalid method arguments.
///
public class InvalidArgumentException extends AbstractPlatformRuntimeException {
    public static final String ERR_NULL_ARGUMENT = "Invalid argument: [%s] should not be null.",
                               ERR_NON_NULL_ARGUMENT = "Invalid argument: [%s] must be null.";

    /// A convenient factory method that either creates a new instance of [InvalidArgumentException] or returns a `cause` if it is already of this type.
    ///
    /// @param msg
    /// @param cause
    /// @return
    ///
    public static InvalidArgumentException wrapIfNecessary(final String msg, final Exception cause) {
        return cause instanceof InvalidArgumentException ? (InvalidArgumentException) cause : new InvalidArgumentException(msg, cause);
    }

    public static <X> X requireNonNull(final X argument, final String name) {
        if (argument == null) {
            throw new InvalidArgumentException(ERR_NULL_ARGUMENT.formatted(name));
        }
        return argument;
    }

    public static void requireNull(final Object argument, final String name) {
        if (argument != null) {
            throw new InvalidArgumentException(ERR_NON_NULL_ARGUMENT.formatted(name));
        }
    }

    public static void requireNotNullArgument(final Object argumentValue, final String argumentName) {
        requireNonNull(argumentValue, argumentName);
    }

    public InvalidArgumentException(final String msg) {
        super(msg);
    }

    public InvalidArgumentException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
