package ua.com.fielden.platform.dao.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * A runtime exception indicating some error that occurred during an attempt to delete entities.
 *
 * @author TG Team
 *
 */
public class EntityDeletionException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public EntityDeletionException(final String msg) {
        super(msg);
    }

    public EntityDeletionException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}