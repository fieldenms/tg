package ua.com.fielden.platform.dao.handlers;

import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * This is a generic contract for handlers of the after save events. It gets invoked strictly after a successful execution of method save of the annotated companion object. The
 * passed into method {@link #perfrom(AbstractEntity)} argument is guaranteed to be the just successfully saved entity.
 * 
 * @author TG Team
 * 
 * @param <T>
 */
public interface IAfterSave<T extends AbstractEntity<?>> {
    void perfrom(final T entity, final List<String> dirtyProperties);
}
