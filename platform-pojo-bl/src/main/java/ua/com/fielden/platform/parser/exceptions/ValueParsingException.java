package ua.com.fielden.platform.parser.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

public class ValueParsingException extends AbstractPlatformRuntimeException {

    private static final String DEFAULT_MESSAGE = "Failed to parse value";

    public ValueParsingException() {
        super(DEFAULT_MESSAGE);
    }

    public ValueParsingException(String s) {
        super(s);
    }

    public ValueParsingException(String message, Throwable cause) {
        super(message != null ? message : DEFAULT_MESSAGE, cause);
    }

}
