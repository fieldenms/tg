package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link TgWebApiEntityWithBooleanPropWithIsGetterAndRecordLikeAccessorCo}.
 *
 * @author TG Team
 *
 */
@EntityType(TgWebApiEntityWithBooleanPropWithIsGetterAndRecordLikeAccessor.class)
public class TgWebApiEntityWithBooleanPropWithIsGetterAndRecordLikeAccessorDao extends CommonEntityDao<TgWebApiEntityWithBooleanPropWithIsGetterAndRecordLikeAccessor> implements TgWebApiEntityWithBooleanPropWithIsGetterAndRecordLikeAccessorCo {

    @Inject
    public TgWebApiEntityWithBooleanPropWithIsGetterAndRecordLikeAccessorDao(final IFilter filter) {
        super(filter);
    }

}