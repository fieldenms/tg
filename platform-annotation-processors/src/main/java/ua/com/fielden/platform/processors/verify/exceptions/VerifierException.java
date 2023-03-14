package ua.com.fielden.platform.processors.verify.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * General exception type to report errors that may occur during model verification process.
 * 
 * @author TG Team
 */
public class VerifierException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public VerifierException(final String msg) {
        super(msg);
    }
    
    public VerifierException(final Throwable cause) {
        super(cause);
    }

    public VerifierException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}