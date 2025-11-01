package ua.com.fielden.platform.entity.query.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/// A generic runtime exception indicating any EQL-related erroneous situation.
/// This call should be extended when implementing other EQL-related exceptions.
///
public class EqlException extends AbstractPlatformRuntimeException {
    public static final String ERR_NULL_ARGUMENT = "Invalid argument: [%s] should not be null.";

    public EqlException(final String msg) {
        super(msg);
    }

    public EqlException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    public static void requireNotNullArgument(final Object argumentValue, final String argumentName) {
        if (argumentValue == null) {
            throw new EqlException(ERR_NULL_ARGUMENT.formatted(argumentName));
        }
    }

}