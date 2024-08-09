package ua.com.fielden.platform.eql.antlr.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * An exception pertaining to situations that occur during parsing, lexing and any other syntactic processing of EQL expressions.
 */
public final class EqlSyntaxException extends AbstractPlatformRuntimeException {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    public EqlSyntaxException(final String s) {
        super(s);
    }

    public EqlSyntaxException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
