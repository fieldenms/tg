package ua.com.fielden.platform.utils;

import java.io.IOException;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

public class StreamCouldNotBeResolvedException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public StreamCouldNotBeResolvedException(final String message, final IOException ioException) {
        super(message, ioException);
    }
}