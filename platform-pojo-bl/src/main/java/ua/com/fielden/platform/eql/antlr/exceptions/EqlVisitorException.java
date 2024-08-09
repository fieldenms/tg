package ua.com.fielden.platform.eql.antlr.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

public final class EqlVisitorException extends AbstractPlatformRuntimeException {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    public EqlVisitorException(String s) {
        super(s);
    }

    public EqlVisitorException(String message, Throwable cause) {
        super(message, cause);
    }

}
