package ua.com.fielden.platform.persistence.types.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

import static java.lang.String.format;

/// A runtime exception that indicates erroneous situation pertaining to the use of Hibernate user types.
///
public class UserTypeException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public static UserTypeException invalidPersistedRepresentation(final String title, final Object value) {
        return new UserTypeException(format(
                "Invalid persisted representation of [%s]. Class: %s. Value: %s",
                title, value == null ? "null" : value.getClass().getName(), value));
    }

    public static UserTypeException invalidJavaRepresentation(final String title, final Object value) {
        return new UserTypeException(format(
                "Invalid Java representation of [%s]. Class: %s. Value: %s",
                title, value == null ? "null" : value.getClass().getName(), value));
    }

    public UserTypeException(final String msg) {
        super(msg);
    }
    
    public UserTypeException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
