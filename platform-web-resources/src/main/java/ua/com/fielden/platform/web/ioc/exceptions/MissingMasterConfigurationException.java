package ua.com.fielden.platform.web.ioc.exceptions;

public class MissingMasterConfigurationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public MissingMasterConfigurationException(final String msg) {
        super(msg);
    }

    public MissingMasterConfigurationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
