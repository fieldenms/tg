package ua.com.fielden.platform.test.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/// A runtime exception that indicates incorrect or unexpected behaviour during a test case construction.
///
public class DomainDrivenTestException extends AbstractPlatformRuntimeException {

    public DomainDrivenTestException(final String msg) {
        super(msg);
    }

    public DomainDrivenTestException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}