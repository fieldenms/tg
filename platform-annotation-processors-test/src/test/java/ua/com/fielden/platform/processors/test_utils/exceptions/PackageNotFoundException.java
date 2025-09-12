package ua.com.fielden.platform.processors.test_utils.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

public class PackageNotFoundException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public PackageNotFoundException(final String message) {
        super(message);
    }

}