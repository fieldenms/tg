package ua.com.fielden.platform.dashboard;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link DashboardRefreshFrequencyUnitCo}.
 *
 * @author TG Team
 *
 */
@EntityType(DashboardRefreshFrequencyUnit.class)
public class DashboardRefreshFrequencyUnitDao extends CommonEntityDao<DashboardRefreshFrequencyUnit> implements DashboardRefreshFrequencyUnitCo {

    @Inject
    public DashboardRefreshFrequencyUnitDao(final IFilter filter) {
        super(filter);
    }

}