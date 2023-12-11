package ua.com.fielden.platform.processors.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * Exception related to initialisation of annotation processors.
 *
 * @author TG Team
 */
public class ProcessorInitializationException extends AbstractPlatformRuntimeException {

    private static final long serialVersionUID = 1L;

    public ProcessorInitializationException(final String msg) {
        super(msg);
    }

    public ProcessorInitializationException(final Throwable cause) {
        super(cause);
    }

    public ProcessorInitializationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
