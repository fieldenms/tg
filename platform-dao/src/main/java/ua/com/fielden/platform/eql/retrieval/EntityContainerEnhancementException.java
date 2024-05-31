package ua.com.fielden.platform.eql.retrieval;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

public final class EntityContainerEnhancementException extends AbstractPlatformRuntimeException {

    public EntityContainerEnhancementException(final String msg) {
        super(msg);
    }

    public EntityContainerEnhancementException(final Throwable cause) {
        super(cause);
    }

    public EntityContainerEnhancementException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
