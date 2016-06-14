package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.ITgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;

import com.google.inject.Inject;

/**
 * DAO for retrieving TgTimesheets.
 * 
 * @author TG Team
 * 
 */
@EntityType(TgFuelUsage.class)
public class TgFuelUsageDao extends CommonEntityDao<TgFuelUsage> implements ITgFuelUsage {

    @Inject
    protected TgFuelUsageDao(final IFilter filter) {
        super(filter);
    }

}
