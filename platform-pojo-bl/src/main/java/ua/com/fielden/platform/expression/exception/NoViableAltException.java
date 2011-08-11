package ua.com.fielden.platform.expression.exception;

import ua.com.fielden.platform.expression.Token;

/**
 * Should be thrown to indicate unrecognised token.
 *
 * @author TG Team
 */
public class NoViableAltException extends RecognitionExceptionWithToken {

    public NoViableAltException(final String msg, final Token token) {
        super(msg, token.beginIndex, token);
    }
}