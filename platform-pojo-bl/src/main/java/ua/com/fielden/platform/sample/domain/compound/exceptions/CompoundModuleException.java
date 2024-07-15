package ua.com.fielden.platform.sample.domain.compound.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * A runtime exception for <code>Compound</code> module.
 *
 * @author TG Team
 *
 */
public class CompoundModuleException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public CompoundModuleException(final String msg) {
        super(msg);
    }

    public CompoundModuleException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}