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
    
    EntityCentreConfig nonConflictingSave(final EntityCentreConfig entity);
    
}