package ua.com.fielden.platform.eql.antlr;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

public final class EqlParseException extends AbstractPlatformRuntimeException {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    public EqlParseException(final String s) {
        super(s);
    }

    public EqlParseException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
