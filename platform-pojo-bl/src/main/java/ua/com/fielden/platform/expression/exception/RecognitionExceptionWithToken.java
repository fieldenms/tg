package ua.com.fielden.platform.expression.exception;

import ua.com.fielden.platform.expression.IExpressionOffendingToken;
import ua.com.fielden.platform.expression.Token;

/**
 * A base exception class for expression parsing errors.
 * The position of an error represented by an instance of this exception in the expression text can be obtained by using method {@link #position()}.
 * An offending token can be accessed using method {@link #token()}.
 *
 * @author TG Team
 */
public abstract class RecognitionExceptionWithToken extends RecognitionException implements IExpressionOffendingToken {

    private final Token token;

    public RecognitionExceptionWithToken(final String msg, final Integer errorPosition, final Token token) {
	super(msg, errorPosition);
	this.token = token;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Token token() {
	return token;
    }
}