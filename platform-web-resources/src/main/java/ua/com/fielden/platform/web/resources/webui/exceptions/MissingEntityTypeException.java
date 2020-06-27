package ua.com.fielden.platform.web.resources.webui.exceptions;

import ua.com.fielden.platform.web.resources.webui.MasterInfoProviderResource;

/**
 * The {@link RuntimeException} that is thrown when {@link MasterInfoProviderResource} can't find the type of entity for which master info was requested
 *
 * @author TG Team
 *
 */
public class MissingEntityTypeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs {@link MissingEntityTypeException} with custom message.
     *
     * @param msg
     */
    public MissingEntityTypeException(final String msg) {
        super(msg);
    }

    /**
     * Constructs {@link MissingEntityTypeException} with custom message and additional {@link Throwable} cause.
     *
     * @param msg
     * @param cause
     */
    public MissingEntityTypeException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
