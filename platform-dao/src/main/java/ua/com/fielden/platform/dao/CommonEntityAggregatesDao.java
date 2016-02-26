package ua.com.fielden.platform.dao;

import java.util.Collections;
import java.util.Map;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

/**
 * DAO implementation for EntityAggregates class used purely for internals of the EntityAggregatesDao.
 *
 * @author TG Team
 *
 */
@EntityType(EntityAggregates.class)
public class CommonEntityAggregatesDao extends CommonEntityDao<EntityAggregates> implements IEntityDao<EntityAggregates> {

    @Inject
    protected CommonEntityAggregatesDao(final IFilter filter) {
        super(filter);
    }

    public int count(final AggregatedResultQueryModel model, final Map<String, Object> paramValues) {
        return evalNumOfPages(model, paramValues, 1).getKey();
    }

    public int count(final AggregatedResultQueryModel model) {
        return count(model, Collections.<String, Object> emptyMap());
    }

}
