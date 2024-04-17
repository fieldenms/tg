package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link TgWebApiEntityWithGetGetterAndRecordLikeAccessorCo}.
 *
 * @author TG Team
 *
 */
@EntityType(TgWebApiEntityWithGetGetterAndRecordLikeAccessor.class)
public class TgWebApiEntityWithGetGetterAndRecordLikeAccessorDao extends CommonEntityDao<TgWebApiEntityWithGetGetterAndRecordLikeAccessor> implements TgWebApiEntityWithGetGetterAndRecordLikeAccessorCo {

    @Inject
    public TgWebApiEntityWithGetGetterAndRecordLikeAccessorDao(final IFilter filter) {
        super(filter);
    }

}