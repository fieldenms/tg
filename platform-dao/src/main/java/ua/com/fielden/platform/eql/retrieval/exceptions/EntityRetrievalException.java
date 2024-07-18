package ua.com.fielden.platform.eql.retrieval.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

public class EntityRetrievalException extends AbstractPlatformRuntimeException {

    public EntityRetrievalException(final String msg) {
        super(msg);
    }

    public EntityRetrievalException(final Throwable cause) {
        super(cause);
    }

    public EntityRetrievalException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
