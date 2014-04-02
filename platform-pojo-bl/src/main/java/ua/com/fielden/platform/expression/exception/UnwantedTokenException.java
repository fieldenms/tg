package ua.com.fielden.platform.expression.exception;

import ua.com.fielden.platform.expression.Token;

/**
 * Indicates that parsing has completed, but some tokens still remain in the tail of the expression.
 * 
 * @author TG Team
 * 
 */
public class UnwantedTokenException extends RecognitionExceptionWithToken {

    public UnwantedTokenException(final String msg, final Token token) {
        super(msg, token.beginIndex, token);
    }

}