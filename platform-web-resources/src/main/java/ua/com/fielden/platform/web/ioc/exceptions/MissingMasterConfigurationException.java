package ua.com.fielden.platform.web.ioc.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

public class MissingMasterConfigurationException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public MissingMasterConfigurationException(final String msg) {
        super(msg);
    }

    public MissingMasterConfigurationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}