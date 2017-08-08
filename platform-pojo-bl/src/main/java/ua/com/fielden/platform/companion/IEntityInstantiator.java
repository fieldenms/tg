package ua.com.fielden.platform.companion;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract to instantiate entities.
 * 
 * @author TG Team
 *
 * @param <T>
 */
public interface IEntityInstantiator<T extends AbstractEntity<?>> {

    /**
     * Instantiates a new entity of the type for which this object is a companion.
     *
     * @return
     */
    T new_();
}
