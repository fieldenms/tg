package ua.com.fielden.platform.audit.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

public class AuditingRuntimeException extends AbstractPlatformRuntimeException {

    private static final long serialVersionUID = 1L;

    public AuditingRuntimeException(final String msg) {
        super(msg);
    }

    public AuditingRuntimeException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
