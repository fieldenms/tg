package ua.com.fielden.platform.expression;

/**
 * A contract which promises to provide the exact position of the error in the expression text.
 * 
 * @author TG Team
 * 
 */
public interface IExpressionErrorPosition {
    /**
     * Returns an index in the expression text indicating the place of an error.
     * 
     * @return
     */
    Integer position();
}
