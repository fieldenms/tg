package ua.com.fielden.platform.eql.antlr.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

public final class EqlCompilationException extends AbstractPlatformRuntimeException {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    public EqlCompilationException(String s) {
        super(s);
    }

    public EqlCompilationException(String message, Throwable cause) {
        super(message, cause);
    }

}
