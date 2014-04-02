package ua.com.fielden.platform.expression.exception;

import ua.com.fielden.platform.expression.Token;

/**
 * Should be thrown to indicate that a reserved name is used for a token.
 * 
 * @author TG Team
 */
public class ReservedNameException extends RecognitionExceptionWithToken {

    public ReservedNameException(final String msg, final Token token) {
        super(msg, token.beginIndex, token);
    }
}