package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link TgEntityWithManyPropTypesCo}.
 * 
 * @author Developers
 * 
 */
@EntityType(TgEntityWithManyPropTypes.class)
public class TgEntityWithManyPropTypesDao extends CommonEntityDao<TgEntityWithManyPropTypes> implements TgEntityWithManyPropTypesCo {

    @Inject
    protected TgEntityWithManyPropTypesDao(IFilter filter) {
        super(filter);
    }
}