package ua.com.fielden.platform.ioc.session.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * A runtime exception that indicates the fact that a database transaction was rolled back due a caught {@link Throwable} that is not {@link Exception}.
 * 
 * @author TG Team
 *
 */
public class TransactionRollbackDueToThrowable extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public TransactionRollbackDueToThrowable(final Throwable cause) {
        super(cause);
    }

}