package ua.com.fielden.platform.web.ioc.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

public class MissingCentreConfigurationException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public MissingCentreConfigurationException(final String msg) {
        super(msg);
    }

    public MissingCentreConfigurationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}