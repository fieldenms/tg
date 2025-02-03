package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;

@EntityType(TgInventoryBin.class)
public class TgInventoryBinDao extends CommonEntityDao<TgInventoryBin> implements TgInventoryBinCo {

    @Inject
    protected TgInventoryBinDao(final IFilter filter) {
        super(filter);
    }

    @Override
    protected IFetchProvider<TgInventoryBin> createFetchProvider() {
        return super.createFetchProvider().with("inventory", "bin", "desc");
    }

}
