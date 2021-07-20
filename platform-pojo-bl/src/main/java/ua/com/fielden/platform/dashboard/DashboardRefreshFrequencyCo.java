package ua.com.fielden.platform.dashboard;

import static ua.com.fielden.platform.utils.EntityUtils.fetch;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

/**
 * Companion object for entity {@link DashboardRefreshFrequency}.
 *
 * @author TG Team
 *
 */
public interface DashboardRefreshFrequencyCo extends IEntityDao<DashboardRefreshFrequency> {
    static final IFetchProvider<DashboardRefreshFrequency> FETCH_PROVIDER = fetch(DashboardRefreshFrequency.class).with("value", "refreshFrequencyUnit");

}