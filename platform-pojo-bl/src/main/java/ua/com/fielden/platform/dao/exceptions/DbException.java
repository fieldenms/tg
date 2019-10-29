package ua.com.fielden.platform.dao.exceptions;

/**
 * Runtime exception that is can be thrown to indicated a database-related error.
 * 
 * @author TG Team
 *
 */
public class DbException extends EntityCompanionException {
    private static final long serialVersionUID = 1L;

    public DbException(final String msg) {
        super(msg);
    }

    public DbException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
