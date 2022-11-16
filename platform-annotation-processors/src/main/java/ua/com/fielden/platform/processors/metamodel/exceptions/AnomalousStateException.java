package ua.com.fielden.platform.processors.metamodel.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

public class AnomalousStateException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public AnomalousStateException(final String message) {
        super(message);
    }
}
