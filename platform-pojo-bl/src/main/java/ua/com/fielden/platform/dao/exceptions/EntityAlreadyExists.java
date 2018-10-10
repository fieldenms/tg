package ua.com.fielden.platform.dao.exceptions;

/**
 * Runtime exception that is thrown when attempting to save an entity as new, but it already exists.
 * 
 * @author TG Team
 *
 */
public class EntityAlreadyExists extends EntityCompanionException {
    private static final long serialVersionUID = 1L;

    public EntityAlreadyExists(final String msg) {
        super(msg);
    }

    public EntityAlreadyExists(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
