package ua.com.fielden.platform.web_api.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * A generic runtime exception to indicate Web API related exceptional situations. 
 * 
 * @author TG Team
 *
 */
public class WebApiException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public WebApiException(final String msg) {
        super(msg);
    }

    public WebApiException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}