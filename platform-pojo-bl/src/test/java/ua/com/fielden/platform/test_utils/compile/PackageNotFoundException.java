package ua.com.fielden.platform.test_utils.compile;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

public class PackageNotFoundException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public PackageNotFoundException(final String message) {
        super(message);
    }

}
