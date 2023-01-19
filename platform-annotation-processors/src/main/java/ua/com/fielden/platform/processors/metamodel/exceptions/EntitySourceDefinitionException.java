package ua.com.fielden.platform.processors.metamodel.exceptions;

/**
 * A runtime exception to report errors pertaining to entity type definitions.
 *
 * @author TG Team
 *
 */
public class EntitySourceDefinitionException extends MetaModelProcessorException {
    private static final long serialVersionUID = 1L;

    public EntitySourceDefinitionException(final String message) {
        super(message);
    }

}