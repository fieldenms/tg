package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

@EntityType(TeProductPriceWithCurrency.class)
public class TeProductPriceWithCurrencyDao extends CommonEntityDao<TeProductPriceWithCurrency> implements TeProductPriceWithCurrencyCo {

    @Override
    protected IFetchProvider<TeProductPriceWithCurrency> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}
