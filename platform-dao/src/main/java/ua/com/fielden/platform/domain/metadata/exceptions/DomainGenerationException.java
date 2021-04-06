package ua.com.fielden.platform.domain.metadata.exceptions;

/**
 * A runtime exception that should be used to capture exceptional situations during domain generation.
 *
 * @author TG Team
 *
 */
public class DomainGenerationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DomainGenerationException(final String msg) {
        super(msg);
    }

    public DomainGenerationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
