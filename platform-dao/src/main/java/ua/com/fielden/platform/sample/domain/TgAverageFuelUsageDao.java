package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.ITgAverageFuelUsage;
import ua.com.fielden.platform.sample.domain.TgAverageFuelUsage;

import com.google.inject.Inject;

@EntityType(TgAverageFuelUsage.class)
public class TgAverageFuelUsageDao extends CommonEntityDao<TgAverageFuelUsage> implements ITgAverageFuelUsage {

    @Inject
    protected TgAverageFuelUsageDao(final IFilter filter) {
        super(filter);
    }
}
