package ua.com.fielden.platform.expression.exception;

import ua.com.fielden.platform.expression.IExpressionErrorPosition;

/**
 * A base exception class for expression parsing errors.
 * The position of an error represented by an instance of this exception in the expression text can be obtained by using method {@link #position()}.
 *
 * @author TG Team
 */
public abstract class RecognitionException extends Exception implements IExpressionErrorPosition {

    private final Integer errorPosition;

    public RecognitionException(final String msg, final Integer errorPosition) {
	super(msg);
	this.errorPosition = errorPosition;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Integer position() {
	return errorPosition;
    }
}