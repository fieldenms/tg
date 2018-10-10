package ua.com.fielden.platform.web.ioc.exceptions;

public class MissingCustomViewConfigurationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public MissingCustomViewConfigurationException(final String msg) {
        super(msg);
    }

    public MissingCustomViewConfigurationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
