package ua.com.fielden.platform.swing.view;

import java.util.Set;

/**
 * A contract for a cache maintaining correspondence between an entity instance ID and a master frame.
 * 
 * @author TG Team
 * 
 */
public interface IEntityMasterCache {
    /**
     * Puts a master frame instance into the cache associating it with a corresponding entity ID.
     * 
     * @param frame
     */
    void put(final BaseFrame frame, final Long entityId);

    /**
     * Should return a master frame associated with the provided entity ID, or null if such association does not exist.
     * 
     * @param entityId
     * @return
     */
    BaseFrame get(final Long entityId);

    /**
     * Should force-remove a master frame associated with the provided entity id from the cache. This operation is required in cases where master for an existing entity is used for
     * creation of a new entity.
     * 
     * @param entityId
     */
    void remove(final Long entityId);

    /**
     * Should return an immutable and synchronised set of available active master frames.
     * 
     * @return
     */
    Set<BaseFrame> all();
}
