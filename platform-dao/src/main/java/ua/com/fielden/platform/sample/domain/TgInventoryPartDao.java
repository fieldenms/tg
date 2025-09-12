package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;

@EntityType(TgInventoryPart.class)
public class TgInventoryPartDao extends CommonEntityDao<TgInventoryPart> implements TgInventoryPartCo {

    @Inject
    protected TgInventoryPartDao(final IFilter filter) {
        super(filter);
    }

    @Override
    protected IFetchProvider<TgInventoryPart> createFetchProvider() {
        return super.createFetchProvider().with("number", "desc");
    }

}
