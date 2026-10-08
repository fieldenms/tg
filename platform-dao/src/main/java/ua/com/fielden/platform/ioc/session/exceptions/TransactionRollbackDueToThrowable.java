package ua.com.fielden.platform.ioc.session.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/// A runtime exception that indicates that a database transaction was rolled back due to a caught [Throwable], which is its cause.
///
/// It is thrown in two cases:
///   - The throwable is not an [Exception], and propagated out of a method annotated with `SessionRequired`.
///   - The throwable propagated out of the traversal of a stream returned by such a method, and the stream is being closed.
///     The throwable may be an exception in this case, as it has propagated already.
///
/// After a [VirtualMachineError], or an exception caused by one, the session is discarded rather than its transaction rolled back, and the database rolls back the transaction.
///
public class TransactionRollbackDueToThrowable extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public TransactionRollbackDueToThrowable(final Throwable cause) {
        super(cause);
    }

}