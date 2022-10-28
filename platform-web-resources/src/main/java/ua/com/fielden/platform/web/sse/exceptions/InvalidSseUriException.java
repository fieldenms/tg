package ua.com.fielden.platform.web.sse.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

public class InvalidSseUriException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidSseUriException(final String msg) {
        super(msg);
    }

    public InvalidSseUriException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}