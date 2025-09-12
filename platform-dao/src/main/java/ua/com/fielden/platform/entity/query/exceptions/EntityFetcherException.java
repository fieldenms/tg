package ua.com.fielden.platform.entity.query.exceptions;

import ua.com.fielden.platform.entity.query.IEntityFetcher;

/// A runtime exception that should be used to capture exceptional situations in {@link IEntityFetcher}.
///
public class EntityFetcherException extends EqlException {

    public EntityFetcherException(final String msg) {
        super(msg);
    }

    public EntityFetcherException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
