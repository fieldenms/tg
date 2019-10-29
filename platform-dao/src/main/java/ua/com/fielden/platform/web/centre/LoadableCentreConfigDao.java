package ua.com.fielden.platform.web.centre;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/** 
 * DAO implementation for companion object {@link ILoadableCentreConfig}.
 * 
 * @author TG Team
 *
 */
@EntityType(LoadableCentreConfig.class)
public class LoadableCentreConfigDao extends CommonEntityDao<LoadableCentreConfig> implements ILoadableCentreConfig {
    
    @Inject
    public LoadableCentreConfigDao(final IFilter filter) {
        super(filter);
    }
    
}