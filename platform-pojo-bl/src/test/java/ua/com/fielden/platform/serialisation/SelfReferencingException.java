package ua.com.fielden.platform.serialisation;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * Exception type that self-references in getter ({@link #getCause()}).
 * <p>
 * See <a href="https://github.com/fieldenms/tg/issues/2379">Issue 2379<a> for more details.
 */
public class SelfReferencingException extends AbstractPlatformRuntimeException {

    public SelfReferencingException(final String message) {
        super(message);
    }

    @Override
    public synchronized Throwable getCause() {
        return this;
    }

}
