package ua.com.fielden.platform.dao.exceptions;

/// Runtime exception that indicates a database-related error.
///
public class DbException extends EntityCompanionException {

    public DbException(final String msg) {
        super(msg);
    }

    public DbException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
