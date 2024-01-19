package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO for {@link ITgOriginatorDetails}
 *
 * @author TG Team
 *
 */
@EntityType(TgOriginatorDetails.class)
public class TgOriginatorDetailsDao extends CommonEntityDao<TgOriginatorDetails> implements ITgOriginatorDetails {

    @Inject
    protected TgOriginatorDetailsDao(final IFilter filter) {
        super(filter);
    }

    @Override
    protected IFetchProvider<TgOriginatorDetails> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}
