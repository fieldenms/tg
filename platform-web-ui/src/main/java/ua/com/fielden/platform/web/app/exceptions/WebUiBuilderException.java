package ua.com.fielden.platform.web.app.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;
import ua.com.fielden.platform.web.app.config.WebUiBuilder;

/**
 * Exception for {@link WebUiBuilder}.
 * 
 * @author TG Team
 *
 */
public class WebUiBuilderException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public WebUiBuilderException(final String message) {
        super(message);
    }

}