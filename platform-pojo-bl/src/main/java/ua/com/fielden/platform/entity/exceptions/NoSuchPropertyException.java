package ua.com.fielden.platform.entity.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

import javax.annotation.Nullable;

/**
 * A runtime exception indicating that a type doesn't have a property with a specified name.
 * 
 * @author TG Team
 */
public class NoSuchPropertyException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public static NoSuchPropertyException noSuchPropertyException(final Class<?> type, final CharSequence property) {
        return noSuchPropertyException(type, property, null);
    }

    public static NoSuchPropertyException noSuchPropertyException(final Class<?> type, final CharSequence property,
                                                                  final @Nullable Throwable cause) {
        return new NoSuchPropertyException("No such property [%s] in type [%s]".formatted(property, type), cause);
    }

    public NoSuchPropertyException(final String msg) {
        super(msg);
    }

    public NoSuchPropertyException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}