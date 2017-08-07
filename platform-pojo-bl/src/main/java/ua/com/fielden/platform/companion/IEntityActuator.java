package ua.com.fielden.platform.companion;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract to actuate (save/execute) entities.
 * 
 * @author TG Team
 *
 * @param <T>
 */
public interface IEntityActuator<T extends AbstractEntity<?>> {

    /**
     * This method is an actuator, which triggers the entity <code>execution</code>.
     * The nature of the <code>execution</code> depends on whether entity is functional or persistent:
     * <ul>
     * <li> For a persistent entity, executing an entity means persisting it (saving or updating).<br>
     * <li> For a functional entity, executing an entity means runs the function that is represented by that entity. It may or may not have a side effect.   
     * <li>
     * </ul>
     * 
     * The passed in and the returned entity instances must NOT be considered reference equivalent.<br>
     * The returned entity should be thought of as a newer version of the passed in instance and used everywhere in the downstream logic of the callee, while the passed in entity should be discarded.
     * 
     * @param entity
     * @return
     */
    T save(final T entity);
    
}
