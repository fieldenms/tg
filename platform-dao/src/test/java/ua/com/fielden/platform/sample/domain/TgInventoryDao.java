package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;

@EntityType(TgInventory.class)
public class TgInventoryDao extends CommonEntityDao<TgInventory> implements TgInventoryCo {

    @Inject
    protected TgInventoryDao(final IFilter filter) {
        super(filter);
    }

    @Override
    protected IFetchProvider<TgInventory> createFetchProvider() {
        return super.createFetchProvider().with("inventoryPart", "desc");
    }

}
