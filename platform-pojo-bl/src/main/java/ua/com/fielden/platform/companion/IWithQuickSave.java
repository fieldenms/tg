package ua.com.fielden.platform.companion;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A part of the mutator contract that provides method <code>quickSave</code> for saving of persistent entities without the need to refetch them.
 * 
 * @author TG Team
 *
 * @param <T>
 */
public interface IWithQuickSave<T extends AbstractEntity<?>> {

    /**
     * Similar to method {@link IEntitySaver#save(AbstractEntity)}, but applicable only to persistent entities. 
     * It returns an <code>id</code> of the saved entity.
     * The implication is that this method should execute faster by skipping the steps required to re-fetch the resultant entity.
     * <p>
     * This method is relevant only for simple cases where method <code>save</code> is not overridden to provide an application specific logic.
     * 
     * @param entity
     * @return
     */
    default long quickSave(final T entity) {
        throw new UnsupportedOperationException(); 
    }

}
