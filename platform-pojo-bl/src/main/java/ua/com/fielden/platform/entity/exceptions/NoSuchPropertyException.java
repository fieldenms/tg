package ua.com.fielden.platform.entity.exceptions;

import jakarta.annotation.Nullable;
import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * A runtime exception indicating that a type doesn't have a property with a specified name.
 * 
 * @author TG Team
 */
public class NoSuchPropertyException extends AbstractPlatformRuntimeException {

    public static final String ERR_NO_SUCH_PROP = "No such property [%s] in type [%s].";

    public static NoSuchPropertyException noSuchPropertyException(final Class<?> type, final CharSequence property) {
        return noSuchPropertyException(type, property, null);
    }

    public static NoSuchPropertyException noSuchPropertyException(
            final Class<?> type,
            final CharSequence property,
            final @Nullable Throwable cause)
    {
        final var typeName = type == null ? "null" : type.getSimpleName();
        return new NoSuchPropertyException(ERR_NO_SUCH_PROP.formatted(property, typeName), cause);
    }

    public NoSuchPropertyException(final String msg) {
        super(msg);
    }

    public NoSuchPropertyException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
