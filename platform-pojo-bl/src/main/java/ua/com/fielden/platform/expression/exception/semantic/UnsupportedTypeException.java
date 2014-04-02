package ua.com.fielden.platform.expression.exception.semantic;

import ua.com.fielden.platform.expression.Token;

/**
 * An error indicating semantic type exception.
 * 
 * @author TG Team
 * 
 */
public class UnsupportedTypeException extends SemanticException {

    private final Class<?> offendingType;

    /**
     * This constructor should be used when a custom error message is preferable to the default one.
     * 
     * @param msg
     *            -- custom error message
     * @param offendingType
     *            -- type of the operand or property that cases problem
     * @param token
     *            -- token, which is offended by specified type
     */
    public UnsupportedTypeException(final String msg, final Class<?> offendingType, final Token token) {
        super(msg, token.beginIndex, token);
        this.offendingType = offendingType;
    }

    /**
     * A shorter version of the exception constructor, which provides a default error message.
     * 
     * @param offendingType
     * @param token
     */
    public UnsupportedTypeException(final Class<?> offendingType, final Token token) {
        this("Type " + offendingType.getName() + " is not supported here.", offendingType, token);
    }

    public Class<?> getOffendingType() {
        return offendingType;
    }

}
