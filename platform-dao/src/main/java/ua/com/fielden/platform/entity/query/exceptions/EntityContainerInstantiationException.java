package ua.com.fielden.platform.entity.query.exceptions;

import ua.com.fielden.platform.entity.query.EntityContainer;
import ua.com.fielden.platform.entity.query.EntityFromContainerInstantiator;

/// Represents an error during instantiation of an entity container.
///
/// @see EntityContainer
/// @see EntityFromContainerInstantiator
///
public final class EntityContainerInstantiationException extends EqlException {

    public EntityContainerInstantiationException(final String s) {
        super(s);
    }

    public EntityContainerInstantiationException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
