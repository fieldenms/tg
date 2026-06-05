package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

import static ua.com.fielden.platform.utils.EntityUtils.fetch;

public interface ITgVehicleFinDetails extends IEntityDao<TgVehicleFinDetails> {

    IFetchProvider<TgVehicleFinDetails> FETCH_PROVIDER = fetch(TgVehicleFinDetails.class)
            .with(AbstractEntity.KEY, "capitalWorksNo");

}
