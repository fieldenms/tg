package ua.com.fielden.platform.processors.metamodel.exceptions;

/**
 * An exception time to report error pertaining to domain meta-model generation.
 *
 * @author TG Team
 *
 */
public class EntityMetaModelException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EntityMetaModelException(final String message) {
        super(message);
    }

}