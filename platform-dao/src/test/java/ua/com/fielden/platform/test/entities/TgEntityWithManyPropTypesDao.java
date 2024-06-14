package ua.com.fielden.platform.test.entities;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link TgEntityWithManyPropTypesCo}.
 * 
 * @author TG Team
 * 
 */
@EntityType(TgEntityWithManyPropTypes.class)
public class TgEntityWithManyPropTypesDao extends CommonEntityDao<TgEntityWithManyPropTypes> implements TgEntityWithManyPropTypesCo {

    @Inject
    protected TgEntityWithManyPropTypesDao(IFilter filter) {
        super(filter);
    }
}