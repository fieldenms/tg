package ua.com.fielden.platform.dao;

import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.IEntityAggregates;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;

import java.util.Collections;
import java.util.Map;

/**
 * DAO implementation for EntityAggregates class used purely for internals of the EntityAggregatesDao.
 *
 * @author TG Team
 *
 */
@EntityType(EntityAggregates.class)
public class CommonEntityAggregatesDao extends CommonEntityDao<EntityAggregates> implements IEntityAggregates {

    public int count(final AggregatedResultQueryModel model, final Map<String, Object> paramValues) {
        return evalNumOfPages(model, paramValues, 1).getKey();
    }

    public int count(final AggregatedResultQueryModel model) {
        return count(model, Collections.<String, Object> emptyMap());
    }

}
