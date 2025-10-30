package ua.com.fielden.platform.test.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * A runtime exception that indicates incorrect or unexpected behaviour during a test case construction.
 * 
 * @author TG Team
 *
 */
public class DomainDriventTestException extends AbstractPlatformRuntimeException {

    public DomainDriventTestException(final String msg) {
        super(msg);
    }

    public DomainDriventTestException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}