package ua.com.fielden.platform.web.centre;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link ICentreColumnWidthConfigUpdater}.
 *
 * @author TG Team
 *
 */
@EntityType(CentreColumnWidthConfigUpdater.class)
public class CentreColumnWidthConfigUpdaterDao extends CommonEntityDao<CentreColumnWidthConfigUpdater> implements ICentreColumnWidthConfigUpdater {
    
    @Inject
    public CentreColumnWidthConfigUpdaterDao(final IFilter filter) {
        super(filter);
    }
    
}