package ua.com.fielden.platform.ioc.session.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/// A runtime exception that indicates a failure to commit a database transaction.
///
/// A unit of work that ends in this exception may not have been persisted, and must not be treated as successful.
/// Whether it was depends on where committing failed: before the JDBC commit, the transaction was rolled back;
/// in the JDBC commit itself, its outcome is unknown, as the database may have committed it before the failure was reported;
/// after the JDBC commit, for example, in a synchronization that Hibernate notifies, the unit of work was persisted.
/// It is distinct from a business failure: the work itself was valid and its statements were accepted,
/// but the transaction could not be made durable, typically due to an infrastructure failure such as
/// a database failover, a connection-pool eviction or a terminated backend.
///
public class TransactionCommitException extends AbstractPlatformRuntimeException {

    public TransactionCommitException(final String msg) {
        super(msg);
    }

    public TransactionCommitException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
