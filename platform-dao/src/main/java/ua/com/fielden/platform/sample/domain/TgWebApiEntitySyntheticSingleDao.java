package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link ITgWebApiEntitySyntheticSingle}.
 *
 * @author TG Team
 *
 */
@EntityType(TgWebApiEntitySyntheticSingle.class)
public class TgWebApiEntitySyntheticSingleDao extends CommonEntityDao<TgWebApiEntitySyntheticSingle> implements ITgWebApiEntitySyntheticSingle {
    
    @Inject
    public TgWebApiEntitySyntheticSingleDao(final IFilter filter) {
        super(filter);
    }
    
}