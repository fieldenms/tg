package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link TgWebApiEntityWithBooleanPropWithIsAndGetGettersCo}.
 *
 * @author TG Team
 *
 */
@EntityType(TgWebApiEntityWithBooleanPropWithIsAndGetGetters.class)
public class TgWebApiEntityWithBooleanPropWithIsAndGetGettersDao extends CommonEntityDao<TgWebApiEntityWithBooleanPropWithIsAndGetGetters> implements TgWebApiEntityWithBooleanPropWithIsAndGetGettersCo {

    @Inject
    public TgWebApiEntityWithBooleanPropWithIsAndGetGettersDao(final IFilter filter) {
        super(filter);
    }

}