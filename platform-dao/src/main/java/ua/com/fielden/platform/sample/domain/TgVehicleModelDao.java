package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

@EntityType(TgVehicleModel.class)
public class TgVehicleModelDao extends CommonEntityDao<TgVehicleModel> implements ITgVehicleModel {

    @Override
    protected IFetchProvider<TgVehicleModel> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}
