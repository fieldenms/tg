package ua.com.fielden.platform.web.minijs.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * A runtime exception that indicates erroneous situation pertaining to JavaScript code.
 *
 * @author TG Team
 *
 */
public class JsCodeException extends AbstractPlatformRuntimeException {

    public JsCodeException(final String msg) {
        super(msg);
    }

    public JsCodeException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}