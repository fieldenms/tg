package ua.com.fielden.platform.dao.exceptions;

/**
 * Runtime exception that is thrown when attempting to save an existing entity, but {@code javax.persistence.OptimisticLockException} was thrown.
 * 
 * @author TG Team
 *
 */
public class EntityWasUpdatedOrDeletedConcurrently extends EntityCompanionException {
    private static final long serialVersionUID = 1L;

    public EntityWasUpdatedOrDeletedConcurrently(final String msg) {
        super(msg);
    }

    public EntityWasUpdatedOrDeletedConcurrently(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}