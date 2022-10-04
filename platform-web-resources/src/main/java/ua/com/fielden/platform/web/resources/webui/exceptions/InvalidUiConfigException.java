package ua.com.fielden.platform.web.resources.webui.exceptions;

public class InvalidUiConfigException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public InvalidUiConfigException(final String msg) {
        super(msg);
    }
    
    public InvalidUiConfigException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}