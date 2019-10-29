package ua.com.fielden.platform.sample.domain.compound.exceptions;

/**
 * A runtime exception for <code>Compound</code> module.
 *
 * @author TG Team
 *
 */
public class CompoundModuleException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CompoundModuleException(final String msg) {
        super(msg);
    }

    public CompoundModuleException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}