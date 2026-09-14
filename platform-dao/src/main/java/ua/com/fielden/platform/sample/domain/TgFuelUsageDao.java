package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

@EntityType(TgFuelUsage.class)
public class TgFuelUsageDao extends CommonEntityDao<TgFuelUsage> implements ITgFuelUsage {

    @Override
    protected IFetchProvider<TgFuelUsage> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}
