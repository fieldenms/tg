package ua.com.fielden.platform.entity.query.exceptions;

import ua.com.fielden.platform.entity.query.IEntityFetcher;

/**
 * A runtime exception that should be used to capture exceptional situations in {@link IEntityFetcher}.
 * 
 * @author TG Team
 *
 */
public class EntityFetcherException extends EqlException {
    private static final long serialVersionUID = 1L;

    public EntityFetcherException(final String msg) {
        super(msg);
    }

    public EntityFetcherException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
