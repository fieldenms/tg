package ua.com.fielden.platform.entity.query.exceptions;

/**
 * A runtime exception indicating validation errors in EQL statements.
 * 
 * @author TG Team
 *
 */
public class EqlValidationException extends EqlException {
    public static final String ERR_LIMIT_GREATER_THAN_ZERO = "Limit must be greater than zero, but was: %s.";
    public static final String ERR_OFFSET_NON_NEGATIVE = "Offset must be a non-negative integer, but was: %s.";

    public EqlValidationException(final String msg) {
        super(msg);
    }

    public EqlValidationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}