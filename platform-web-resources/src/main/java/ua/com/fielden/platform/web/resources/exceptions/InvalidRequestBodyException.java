package ua.com.fielden.platform.web.resources.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/// Indicates that the body of a request could not be deserialised.
///
/// The exception carries a generic message and no cause, as it is serialised to the client.
/// The underlying failure is logged on the server.
///
public class InvalidRequestBodyException extends AbstractPlatformRuntimeException {

    public InvalidRequestBodyException(final String msg) {
        super(msg);
    }

}
