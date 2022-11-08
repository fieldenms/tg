package ua.com.fielden.platform.processors.exception;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

public class ProcessingException extends AbstractPlatformRuntimeException {

    private static final long serialVersionUID = 1L;

    public ProcessingException(final String msg) {
        super(msg);
    }

    public ProcessingException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
