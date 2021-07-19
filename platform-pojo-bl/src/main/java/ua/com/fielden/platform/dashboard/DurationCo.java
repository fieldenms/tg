package ua.com.fielden.platform.dashboard;

import static ua.com.fielden.platform.utils.EntityUtils.fetch;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

/**
 * Companion object for entity {@link Duration}.
 *
 * @author TG Team
 *
 */
public interface DurationCo extends IEntityDao<Duration> {
    static final IFetchProvider<Duration> FETCH_PROVIDER = fetch(Duration.class).with("count", "durationUnit");

}