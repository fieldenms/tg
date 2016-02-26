package ua.com.fielden.platform.dao.exceptions;

/**
 * Runtime exception that should be thrown from within entity companion implementation.
 * 
 * @author TG Team
 *
 */
public class EntityCompanionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EntityCompanionException(final String msg) {
        super(msg);
    }
}
