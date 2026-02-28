package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;

@EntityType(TgVehicleFinDetails.class)
public class TgVehicleFinDetailsDao extends CommonEntityDao<TgVehicleFinDetails> implements ITgVehicleFinDetails {

    @Inject
    protected TgVehicleFinDetailsDao(final IFilter filter) {
        super(filter);
    }

    @Override
    protected IFetchProvider<TgVehicleFinDetails> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}
