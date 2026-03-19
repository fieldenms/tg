package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

import static ua.com.fielden.platform.utils.EntityUtils.fetch;

public interface TeProductPriceWithCurrencyCo extends IEntityDao<TeProductPriceWithCurrency> {

    IFetchProvider<TeProductPriceWithCurrency> FETCH_PROVIDER = fetch(TeProductPriceWithCurrency.class)
            .with("product", "price", "other");

}
