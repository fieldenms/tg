package ua.com.fielden.platform.expression.exception.semantic;

import ua.com.fielden.platform.expression.Token;

/**
 * An error indicating that the specified property cannot be used.
 *
 * @author TG Team
 *
 */
public class InvalidPropertyException extends SemanticException {

    public InvalidPropertyException(final String msg, final Token token) {
	super(msg, token.beginIndex, token);
    }

}
