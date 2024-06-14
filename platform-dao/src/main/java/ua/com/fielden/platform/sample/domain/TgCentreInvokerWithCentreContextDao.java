package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link ITgCentreInvokerWithCentreContext}.
 *
 * @author Developers
 *
 */
@EntityType(TgCentreInvokerWithCentreContext.class)
public class TgCentreInvokerWithCentreContextDao extends CommonEntityDao<TgCentreInvokerWithCentreContext> implements ITgCentreInvokerWithCentreContext {

    @Inject
    public TgCentreInvokerWithCentreContextDao(final IFilter filter) {
        super(filter);
    }

    @Override
    public IFetchProvider<TgCentreInvokerWithCentreContext> createFetchProvider() {
        return super.createFetchProvider()
                .with("key") // this property is "required" (necessary during saving) -- should be declared as fetching property
                .with("desc");
    }
}