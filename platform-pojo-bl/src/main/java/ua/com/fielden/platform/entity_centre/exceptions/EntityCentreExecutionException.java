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

    public EntityCentreExecutionException(final String msg) {
        super(msg);
    }

    public EntityCentreExecutionException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}