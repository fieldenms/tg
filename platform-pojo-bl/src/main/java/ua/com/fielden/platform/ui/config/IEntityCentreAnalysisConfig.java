package ua.com.fielden.platform.ui.config;

import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.dao.IEntityDao;

import ua.com.fielden.platform.dao.IMasterDetailsDao;

/** 
 * Companion object for entity {@link EntityCentreAnalysisConfig}.
 * 
 * @author Developers
 *
 */
public interface IEntityCentreAnalysisConfig extends IEntityDao<EntityCentreAnalysisConfig>, IMasterDetailsDao<EntityCentreConfig, EntityCentreAnalysisConfig> {

}