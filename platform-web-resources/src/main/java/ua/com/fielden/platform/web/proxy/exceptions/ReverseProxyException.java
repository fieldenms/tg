package ua.com.fielden.platform.web.proxy.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

public class ReverseProxyException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public ReverseProxyException(final String msg) {
        super(msg);
    }

    public ReverseProxyException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}