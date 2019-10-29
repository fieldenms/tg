package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO for {@link ITgAuthoriser}
 *
 * @author TG Team
 *
 */
@EntityType(TgAuthoriser.class)
public class TgAuthoriserDao extends CommonEntityDao<TgAuthoriser> implements ITgAuthoriser {

    @Inject
    protected TgAuthoriserDao(final IFilter filter) {
        super(filter);
    }
    

    @Override
    protected IFetchProvider<TgAuthoriser> createFetchProvider() {
        return super.createFetchProvider().with("person", "category");
    }
}
