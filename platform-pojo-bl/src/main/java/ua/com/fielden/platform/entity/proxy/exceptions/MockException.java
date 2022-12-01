package ua.com.fielden.platform.entity.proxy.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * The exception that may happen during actions with mock entity like setting field value.
 *
 * @author TG Team
 *
 */
public class MockException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public MockException(final String msg) {
        super(msg);
    }

    public MockException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
