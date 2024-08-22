package ua.com.fielden.platform.entity.query.model;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

public class FillModelException extends AbstractPlatformRuntimeException {

    private static final long serialVersionUID = 1L;

    public FillModelException(final String msg) {
        super(msg);
    }

    public FillModelException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
