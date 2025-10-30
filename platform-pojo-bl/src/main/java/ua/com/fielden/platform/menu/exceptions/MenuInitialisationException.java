package ua.com.fielden.platform.menu.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/// Indicates a problem in Menu initialisation process.
///
public class MenuInitialisationException extends AbstractPlatformRuntimeException {

    public MenuInitialisationException(final String msg) {
        super(msg);
    }

    public MenuInitialisationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
