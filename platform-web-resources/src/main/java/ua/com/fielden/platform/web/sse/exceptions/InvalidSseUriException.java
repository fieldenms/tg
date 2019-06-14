package ua.com.fielden.platform.web.sse.exceptions;

public class InvalidSseUriException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public InvalidSseUriException(final String msg) {
        super(msg);
    }
    
    public InvalidSseUriException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}