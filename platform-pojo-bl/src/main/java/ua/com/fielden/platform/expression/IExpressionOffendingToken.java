package ua.com.fielden.platform.expression;

/**
 * A contract which promises to provide a token, which offends an expression.
 *
 * @author TG Team
 *
 */
public interface IExpressionOffendingToken {
    /**
     * Returns an offending token.
     *
     * @return
     */
    Token token();
}
