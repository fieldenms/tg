package ua.com.fielden.platform.web.ioc.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

public class MissingCustomViewConfigurationException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public MissingCustomViewConfigurationException(final String msg) {
        super(msg);
    }

    public MissingCustomViewConfigurationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}