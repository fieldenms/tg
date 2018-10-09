package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO for {@link ITgOriginator}
 *
 * @author TG Team
 *
 */
@EntityType(TgOriginator.class)
public class TgOriginatorDao extends CommonEntityDao<TgOriginator> implements ITgOriginator {

    @Inject
    protected TgOriginatorDao(final IFilter filter) {
        super(filter);
    }

    @Override
    protected IFetchProvider<TgOriginator> createFetchProvider() {
        return super.createFetchProvider().with("person", "assistant");
    }

}
