package ua.com.fielden.platform.processors.metamodel.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * The most general exception type to report error that may occur during annotation processing as part of meta-model generation.
 * More specific exception types should be used to report errors more precisely. Such exception type should extend {@link MetaModelProcessorException}.
 * 
 * @author TG Team
 *
 */
public class MetaModelProcessorException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public MetaModelProcessorException(final String msg) {
        super(msg);
    }
    
    public MetaModelProcessorException(final Throwable cause) {
        super(cause);
    }

    public MetaModelProcessorException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}