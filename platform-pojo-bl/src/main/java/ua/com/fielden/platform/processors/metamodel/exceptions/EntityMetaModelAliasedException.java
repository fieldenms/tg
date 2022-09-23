package ua.com.fielden.platform.processors.metamodel.exceptions;

/**
 * An exception time to report error pertaining to domain meta-model generation.
 *
 * @author TG Team
 *
 */
public class EntityMetaModelAliasedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EntityMetaModelAliasedException(final String message) {
        super(message);
    }

}