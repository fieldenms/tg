package ua.com.fielden.platform.expression.exception.semantic;

import ua.com.fielden.platform.expression.Token;

/**
 * An error indicating incorrect number of operation operands.
 * 
 * @author TG Team
 * 
 */
public class UnexpectedNumberOfOperandsException extends SemanticException {

    public UnexpectedNumberOfOperandsException(final String msg, final Token token) {
        super(msg, token.beginIndex, token);
    }

}
