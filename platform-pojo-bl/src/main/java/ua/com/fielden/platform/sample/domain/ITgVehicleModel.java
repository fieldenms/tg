package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

import static ua.com.fielden.platform.utils.EntityUtils.fetch;

public interface ITgVehicleModel extends IEntityDao<TgVehicleModel> {

    IFetchProvider<TgVehicleModel> FETCH_PROVIDER = fetch(TgVehicleModel.class).with(
            AbstractEntity.KEY,
            AbstractEntity.DESC,
            "make",
            "ordinaryIntProp"
    );

}
