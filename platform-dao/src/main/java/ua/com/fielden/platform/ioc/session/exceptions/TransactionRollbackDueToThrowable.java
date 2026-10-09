package ua.com.fielden.platform.ioc.session.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/// A runtime exception that indicates that a database transaction was rolled back due to a caught [Throwable].
///
/// It is thrown in two cases:
///   - The throwable is not an [Exception], and propagated out of a method annotated with `SessionRequired`.
///     The throwable is its cause.
///   - The throwable propagated out of the traversal of a stream returned by such a method, and the stream is being closed.
///     The throwable may be an exception in this case, as it has propagated already, and is referred to in the message, rather than as the cause.
///     Try-with-resources attaches this exception to the throwable as a suppressed exception, so the throwable would otherwise refer to itself through it,
///     a cycle that breaks serialising either of them, for example, to JSON.
///
/// After a [VirtualMachineError], or an exception caused by one, the session is discarded rather than its transaction rolled back, and the database rolls back the transaction.
///
public class TransactionRollbackDueToThrowable extends AbstractPlatformRuntimeException {

    public TransactionRollbackDueToThrowable(final Throwable cause) {
        super(cause);
    }

    public TransactionRollbackDueToThrowable(final String message) {
        super(message);
    }

}