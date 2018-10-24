package ua.com.fielden.platform.ui.config.api;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;

/**
 * Companion object for entity {@link EntityCentreConfig}.
 * 
 * @author TG Team
 * 
 */
public interface IEntityCentreConfig extends IEntityDao<EntityCentreConfig> {
    
    /**
     * Saves the entity in repeating manner until the process is successfully concluded.<br>
     * Only conflicting errors will trigger saving again.
     * <p>
     * VERY IMPORTANT: this must be used outside of another transaction scopes.
     * 
     * @param entity
     * @return
     */
    EntityCentreConfig saveWithoutConflicts(final EntityCentreConfig entity);
    
    /**
     * Saves the entity in a regular manner with conflict check. Can be nested inside other transaction scopes.
     * 
     * @param entity
     * @return
     */
    EntityCentreConfig saveWithConflicts(final EntityCentreConfig entity);
    
}