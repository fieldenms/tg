package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link TgWebApiEntityWithBooleanPropWithRecordLikeAccessorOnlyCo}.
 *
 * @author TG Team
 *
 */
@EntityType(TgWebApiEntityWithBooleanPropWithRecordLikeAccessorOnly.class)
public class TgWebApiEntityWithBooleanPropWithRecordLikeAccessorOnlyDao extends CommonEntityDao<TgWebApiEntityWithBooleanPropWithRecordLikeAccessorOnly> implements TgWebApiEntityWithBooleanPropWithRecordLikeAccessorOnlyCo {

    @Inject
    public TgWebApiEntityWithBooleanPropWithRecordLikeAccessorOnlyDao(final IFilter filter) {
        super(filter);
    }

}