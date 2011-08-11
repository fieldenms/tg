package ua.com.fielden.platform.expression.exception.semantic;

import ua.com.fielden.platform.expression.Token;

/**
 * An error indicating semantic type exception.
 *
 * @author TG Team
 *
 */
public class UnsupportedTypeException extends SemanticException {

    public UnsupportedTypeException(final String msg, final Token token) {
	super(msg, token.beginIndex, token);
    }

}
