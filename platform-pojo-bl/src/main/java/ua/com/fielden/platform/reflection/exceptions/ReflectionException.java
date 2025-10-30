package ua.com.fielden.platform.reflection.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/// A runtime exception to indicate a TG specific reflection related exception.
///
public class ReflectionException extends AbstractPlatformRuntimeException {
    public static final String ERR_NULL_ARGUMENT = "Invalid argument: [%s] should not be null.";
    
    public ReflectionException(final String msg) {
        super(msg);
    }

    public ReflectionException(final Throwable cause) {
        super(cause);
    }

    public ReflectionException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    public static void requireNotNullArgument(final Object argumentValue, final String argumentName) {
        if (argumentValue == null) {
            throw new ReflectionException(ERR_NULL_ARGUMENT.formatted(argumentName));
        }
    }


}