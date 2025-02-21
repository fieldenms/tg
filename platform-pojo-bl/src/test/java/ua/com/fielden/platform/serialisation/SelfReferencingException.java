package ua.com.fielden.platform.serialisation;

/**
 * Exception type that self-references in getter ({@link #getCause()}).
 * See https://github.com/fieldenms/tg/issues/2379 for more details.
 */
public class SelfReferencingException extends RuntimeException {

    public SelfReferencingException(final String message) {
        super(message);
    }

    @Override
    public synchronized Throwable getCause() {
        return this;
    }

}
