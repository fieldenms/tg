package ua.com.fielden.platform.web.sse.exceptions;

/**
 * A runtime exception to report SSE related errors.
 * 
 * @author TG Team
 *
 */
public class SseException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public SseException(final String msg) {
        super(msg);
    }
    
    public SseException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}