package ua.com.fielden.platform.web.ioc.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/// An exception to report missing web resources. It should be used only if more specialised exceptions are not available.
///
public class MissingWebResourceException extends AbstractPlatformRuntimeException {

    public MissingWebResourceException(final String msg) {
        super(msg);
    }

    public MissingWebResourceException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}