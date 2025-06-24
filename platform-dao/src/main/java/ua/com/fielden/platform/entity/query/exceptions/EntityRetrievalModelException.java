package ua.com.fielden.platform.entity.query.exceptions;

import ua.com.fielden.platform.entity.query.EntityRetrievalModel;
import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/// Exception pertaining to [EntityRetrievalModel].
///
public sealed class EntityRetrievalModelException extends AbstractPlatformRuntimeException {

    public EntityRetrievalModelException(final String msg) {
        super(msg);
    }

    public EntityRetrievalModelException(final Throwable cause) {
        super(cause);
    }

    public EntityRetrievalModelException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    /// Represents an exceptional situation during retrieval model construction -- a cycle was detected in an entity graph.
    ///
    public static final class GraphCycle extends EntityRetrievalModelException {

        public GraphCycle(final String msg) {
            super(msg);
        }

    }

}
