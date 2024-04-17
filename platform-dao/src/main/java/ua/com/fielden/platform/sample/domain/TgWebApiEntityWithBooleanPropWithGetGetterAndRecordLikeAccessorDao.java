package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link TgWebApiEntityWithBooleanPropWithGetGetterAndRecordLikeAccessorCo}.
 *
 * @author TG Team
 *
 */
@EntityType(TgWebApiEntityWithBooleanPropWithGetGetterAndRecordLikeAccessor.class)
public class TgWebApiEntityWithBooleanPropWithGetGetterAndRecordLikeAccessorDao extends CommonEntityDao<TgWebApiEntityWithBooleanPropWithGetGetterAndRecordLikeAccessor> implements TgWebApiEntityWithBooleanPropWithGetGetterAndRecordLikeAccessorCo {

    @Inject
    public TgWebApiEntityWithBooleanPropWithGetGetterAndRecordLikeAccessorDao(final IFilter filter) {
        super(filter);
    }

}