package ua.com.fielden.platform.dashboard;

import java.util.Collection;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.persistent.DashboardRefreshFrequency_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.persistent.DashboardRefreshFrequency_CanSave_Token;

/**
 * DAO implementation for companion object {@link DashboardRefreshFrequencyCo}.
 *
 * @author TG Team
 */
@EntityType(DashboardRefreshFrequency.class)
public class DashboardRefreshFrequencyDao extends CommonEntityDao<DashboardRefreshFrequency> implements DashboardRefreshFrequencyCo {

    @Override
    @SessionRequired
    @Authorise(DashboardRefreshFrequency_CanSave_Token.class)
    public DashboardRefreshFrequency save(final DashboardRefreshFrequency entity) {
        return super.save(entity);
    }

    @Override
    @SessionRequired
    @Authorise(DashboardRefreshFrequency_CanDelete_Token.class)
    public int batchDelete(final Collection<Long> entitiesIds) {
        return defaultBatchDelete(entitiesIds);
    }

    @Override
    protected IFetchProvider<DashboardRefreshFrequency> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}
