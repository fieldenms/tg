package ua.com.fielden.platform.entity_centre.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * A runtime exception that indicates incorrect situation pertaining to a failure to save an Entity Centre configuration or incorrect situation during entity centres execution.
 *
 * @author TG Team
 *
 */
public class EntityCentreExecutionException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;
    public static final String ERR_NULL_ARGUMENT = "Invalid argument: [%s] should not be null.";

    public EntityCentreExecutionException(final String msg) {
        super(msg);
    }

    public EntityCentreExecutionException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    public static void requireNotNullArgument(final Object argumentValue, final String argumentName) {
        if (argumentValue == null) {
            throw new EntityCentreExecutionException(ERR_NULL_ARGUMENT.formatted(argumentName));
        }
    }

}