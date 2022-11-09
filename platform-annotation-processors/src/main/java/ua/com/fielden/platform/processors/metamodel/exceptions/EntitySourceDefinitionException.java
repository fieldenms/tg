package ua.com.fielden.platform.processors.metamodel.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformCheckedException;

public class EntitySourceDefinitionException extends AbstractPlatformCheckedException {
    private static final long serialVersionUID = 1L;

    public EntitySourceDefinitionException(final String message) {
        super(message);
    }

}
