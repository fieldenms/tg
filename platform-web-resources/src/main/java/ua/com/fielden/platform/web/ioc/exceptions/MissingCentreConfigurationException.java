package ua.com.fielden.platform.web.ioc.exceptions;

public class MissingCentreConfigurationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public MissingCentreConfigurationException(final String msg) {
        super(msg);
    }

    public MissingCentreConfigurationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
