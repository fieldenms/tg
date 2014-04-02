package ua.com.fielden.platform.expression.exception.semantic;

import ua.com.fielden.platform.expression.IExpressionErrorPosition;
import ua.com.fielden.platform.expression.IExpressionOffendingToken;
import ua.com.fielden.platform.expression.Token;

/**
 * A base exception for all semantic related errors. The position of an error represented by an instance of this exception in the expression text can be obtained by using method
 * {@link #position()}. An offending token can be accessed using method {@link #token()}.
 * 
 * @author TG Team
 * 
 */
public abstract class SemanticException extends Exception implements IExpressionErrorPosition, IExpressionOffendingToken {

    private final Integer errorPosition;
    private final Token token;

    public SemanticException(final String msg, final Integer errorPosition, final Token token) {
        super(msg);
        this.errorPosition = errorPosition;
        this.token = token;
    }

    public SemanticException(final String msg, final Throwable t, final Integer errorPosition, final Token token) {
        super(msg, t);
        this.errorPosition = errorPosition;
        this.token = token;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Integer position() {
        return errorPosition;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Token token() {
        return token;
    }
}
