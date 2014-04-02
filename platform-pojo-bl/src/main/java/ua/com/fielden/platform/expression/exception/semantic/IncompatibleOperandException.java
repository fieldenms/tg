package ua.com.fielden.platform.expression.exception.semantic;

import ua.com.fielden.platform.expression.Token;

/**
 * An error indicating that expression contains incompatible operands.
 * 
 * @author TG Team
 * 
 */
public class IncompatibleOperandException extends SemanticException {

    public IncompatibleOperandException(final String msg, final Token token) {
        super(msg, token.beginIndex, token);
    }

}
