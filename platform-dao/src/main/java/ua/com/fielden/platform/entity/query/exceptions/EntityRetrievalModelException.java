package ua.com.fielden.platform.entity.query.exceptions;

import ua.com.fielden.platform.entity.query.EntityRetrievalModel;

/// Exception pertaining to [EntityRetrievalModel].
///
public final class EntityRetrievalModelException extends EqlException {

    public EntityRetrievalModelException(final String msg) {
        super(msg);
    }

    public EntityRetrievalModelException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
