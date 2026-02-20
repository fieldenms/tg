package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.companion.ISaveWithFetch;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.utils.EntityUtils.fetch;

public interface ITgVehicle extends IEntityDao<TgVehicle>, ISaveWithFetch<TgVehicle> {

    IFetchProvider<TgVehicle> FETCH_PROVIDER = fetch(TgVehicle.class)
            .with(KEY, "model");

}
