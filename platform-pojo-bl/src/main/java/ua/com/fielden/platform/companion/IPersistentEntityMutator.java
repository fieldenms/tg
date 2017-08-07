package ua.com.fielden.platform.companion;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract that combines {@link IPersistentEntityDeleter} and {@link IPersistentEntitySaver} as a convenience in cases where both sets of mutating methods (save and delete) are required.
 * <p>
 * In addition, this contract adds method {@link #quickSave(AbstractEntity)}, which could only be applicable to persistent entities.
 *  
 * @author TG Team
 *
 * @param <T>
 */
public interface IPersistentEntityMutator<T extends AbstractEntity<?>> extends IPersistentEntityDeleter<T>, IEntityActuator<T> {
    
    /**
     * Similar to method {@link IEntityActuator#save(AbstractEntity)}, but applicable only to persistent entities. 
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
