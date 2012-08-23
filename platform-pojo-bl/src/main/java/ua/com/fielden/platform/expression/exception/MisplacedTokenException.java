package ua.com.fielden.platform.expression.exception;

import ua.com.fielden.platform.expression.Token;

/**
 * Should be thrown to indicate that a token in question is used in place where it should not be used.
 * For example, keyword SELF whould only be used as part of function COUNT call.
 *
 * @author TG Team
 */
public class MisplacedTokenException extends RecognitionExceptionWithToken {

    public MisplacedTokenException(final String msg, final Token token) {
        super(msg, token.beginIndex, token);
    }
}