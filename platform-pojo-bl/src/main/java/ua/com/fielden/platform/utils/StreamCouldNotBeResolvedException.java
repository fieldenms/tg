package ua.com.fielden.platform.utils;

import java.io.IOException;

public class StreamCouldNotBeResolvedException extends IllegalArgumentException {
    public StreamCouldNotBeResolvedException(final String message, final IOException ioException) {
        super(message, ioException);
    }
}

