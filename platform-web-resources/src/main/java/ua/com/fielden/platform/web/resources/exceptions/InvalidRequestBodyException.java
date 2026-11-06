package ua.com.fielden.platform.web.resources.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/// Indicates that the body of a request could not be deserialised.
///
/// The exception carries a generic message and no cause, as it is serialised to the client.
/// The underlying failure is logged on the server.
///
/// Resources respond to it with status `400 Bad Request` (see [ua.com.fielden.platform.web.utils.WebUiResourceUtils#handleUndesiredExceptions] and [ua.com.fielden.platform.web.resources.webui.GraphiQLResource]).
///
public class InvalidRequestBodyException extends AbstractPlatformRuntimeException {

    public InvalidRequestBodyException(final String msg) {
        super(msg);
    }

}
