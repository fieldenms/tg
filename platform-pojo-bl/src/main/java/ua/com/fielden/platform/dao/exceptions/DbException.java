package ua.com.fielden.platform.dao.exceptions;

/// Runtime exception that is can be thrown to indicate a database-related error.
///
public class DbException extends EntityCompanionException {

    public DbException(final String msg) {
        super(msg);
    }

    public DbException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}