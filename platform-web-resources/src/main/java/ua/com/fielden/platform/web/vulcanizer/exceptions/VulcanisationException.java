package ua.com.fielden.platform.web.vulcanizer.exceptions;

public class VulcanisationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public VulcanisationException(final String msg) {
        super(msg);
    }
    
    public VulcanisationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}