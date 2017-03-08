package ua.com.fielden.platform.entity.query.exceptions;

import ua.com.fielden.platform.entity.query.EntityFetcher;

/**
 * A runtime exception that should be used to capture exceptional situations in {@link EntityFetcher}.
 * 
 * @author TG Team
 *
 */
public class EntityFetcherException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public EntityFetcherException(final String msg) {
        super(msg);
    }
    
    public EntityFetcherException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
