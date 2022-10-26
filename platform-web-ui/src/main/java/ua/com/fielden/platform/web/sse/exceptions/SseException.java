package ua.com.fielden.platform.web.sse.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * A runtime exception to report SSE related errors.
 * 
 * @author TG Team
 *
 */
public class SseException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public SseException(final String msg) {
        super(msg);
    }

    public SseException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}