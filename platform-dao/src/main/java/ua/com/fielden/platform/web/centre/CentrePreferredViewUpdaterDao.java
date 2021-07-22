package ua.com.fielden.platform.web.centre;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link CentrePreferredViewUpdaterCo}.
 *
 * @author TG Team
 *
 */
@EntityType(CentrePreferredViewUpdater.class)
public class CentrePreferredViewUpdaterDao extends CommonEntityDao<CentrePreferredViewUpdater> implements CentrePreferredViewUpdaterCo {
    
    @Inject
    public CentrePreferredViewUpdaterDao(final IFilter filter) {
        super(filter);
    }
    
}