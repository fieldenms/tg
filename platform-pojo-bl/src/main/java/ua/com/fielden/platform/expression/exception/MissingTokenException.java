package ua.com.fielden.platform.expression.exception;

import ua.com.fielden.platform.expression.Token;

/**
 * Indicates that some token is expected, but missing.
 *
 * @author TG Team
 */
public class MissingTokenException extends RecognitionExceptionWithToken {

    public MissingTokenException(final String msg, final Token token) {
	super(msg, token.endIndex, token);
    }
}