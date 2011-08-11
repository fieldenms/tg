package ua.com.fielden.platform.expression.exception;

import ua.com.fielden.platform.expression.Token;

/**
 * Indicates inconsistency between expected and actual tokens.
 *
 * @author TG Team
 */
public class MismatchedTokenException extends RecognitionExceptionWithToken {

    public MismatchedTokenException(final String msg, final Token token) {
	super(msg, token.beginIndex, token);
    }
}