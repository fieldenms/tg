package ua.com.fielden.platform.ui.config.api;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;

/**
 * Companion object for entity {@link EntityCentreConfig}.
 * <p>
 * Please, do not use standard {@link #save(EntityCentreConfig)} method in client code, use instead the methods below, deciding whether graceful conflict resolution is needed.
 * 
 * @author TG Team
 * 
 */
public interface IEntityCentreConfig extends IEntityDao<EntityCentreConfig> {
    
    /**
     * Saves the entity (quickly) in repeating manner until the process is successfully concluded.<br>
     * Only conflicting errors will trigger saving again.
     * <p>
     * VERY IMPORTANT: this must be used outside of another transaction scopes.
     * 
     * @param entity
     * @return
     */
    Long saveWithoutConflicts(final EntityCentreConfig entity);
    
    /**
     * Saves the entity (quickly) in a regular manner with conflict check. Can be nested inside other transaction scopes.
     * 
     * @param entity
     * @return
     */
    Long saveWithConflicts(final EntityCentreConfig entity);
    
}