package ua.com.fielden.platform.web.centre.api.insertion_points.exception;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * Exception generated during incorrect insertion point configuration
 *
 * @author TG Team
 *
 */
public class InsertionPointConfigException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public InsertionPointConfigException(final String message) {
        super(message);
    }

}