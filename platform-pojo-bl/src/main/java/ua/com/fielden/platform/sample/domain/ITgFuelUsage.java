package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

import static ua.com.fielden.platform.utils.EntityUtils.fetch;

public interface ITgFuelUsage extends IEntityDao<TgFuelUsage> {

    IFetchProvider<TgFuelUsage> FETCH_PROVIDER = fetch(TgFuelUsage.class).with(
            "vehicle",
            "date",
            "qty",
            "pricePerLitre",
            "fuelType");

}
