package ua.com.fielden.platform.expression.exception.semantic;

import ua.com.fielden.platform.expression.Token;

/**
 * An error indicating that the specified property does not exists.
 *
 * @author TG Team
 *
 */
public class MissingPropertyException extends SemanticException {

    public MissingPropertyException(final String msg, final Token token) {
	super(msg, token.beginIndex, token);
    }

    public MissingPropertyException(final String msg, final Throwable t, final Token token) {
	super(msg, t, token.beginIndex, token);
    }

}
