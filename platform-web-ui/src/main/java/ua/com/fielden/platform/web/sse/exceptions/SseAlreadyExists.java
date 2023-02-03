package ua.com.fielden.platform.web.sse.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

public class SseAlreadyExists extends AbstractPlatformRuntimeException {

    private static final long serialVersionUID = 1L;

    public SseAlreadyExists(final String msg) {
        super(msg);
    }

    public SseAlreadyExists(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
