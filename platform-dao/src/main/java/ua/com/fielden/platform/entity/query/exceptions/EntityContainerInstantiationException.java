package ua.com.fielden.platform.entity.query.exceptions;

import ua.com.fielden.platform.entity.query.EntityContainer;
import ua.com.fielden.platform.entity.query.EntityFromContainerInstantiator;
import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * Represents an error during instantiation of an entity container.
 *
 * @see EntityContainer
 * @see EntityFromContainerInstantiator
 */
public final class EntityContainerInstantiationException extends AbstractPlatformRuntimeException {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    public EntityContainerInstantiationException(final String s) {
        super(s);
    }

    public EntityContainerInstantiationException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
