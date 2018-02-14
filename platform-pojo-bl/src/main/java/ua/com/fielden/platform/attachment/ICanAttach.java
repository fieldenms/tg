package ua.com.fielden.platform.attachment;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;

/**
 * A contract for associating entities of type <code>T</code> by means of creating association instances of type <code>A</code>.
 *
 * @param <T> -- entity to be associated with attachments
 * 
 * @author TG Team
 */
public interface ICanAttach<T extends AbstractPersistentEntity<?>> {
    
    /**
     * Associates <code>attachment</code> with <code>entity</code> and returns the resultant association already persisted.
     * <p>
     * This method should handle the situation where a corresponding association may already exists.
     * There is no need to throw any exceptions in such cases -- simply return existing association. 
     *
     * @param <A> -- entity type representing attachment association
     * @param attachment
     * @param entity
     * @return persisted association instance
     */
    <A extends AbstractPersistentEntity<?>> A attach(final Attachment attachment, final T entity);
}
