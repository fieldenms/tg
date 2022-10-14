package ua.com.fielden.platform.processors.test_utils.exceptions;

public class PackageNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PackageNotFoundException(final String message) {
        super(message);
    }

}