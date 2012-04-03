package ua.com.fielden.platform.dao;

import java.util.Collections;
import java.util.Map;

import ua.com.fielden.platform.dao2.CommonEntityDao2;
import ua.com.fielden.platform.dao2.IEntityDao2;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO implementation for EntityAggregates class used purely for internals of the EntityAggregatesDao.
 * @author TG Team
 *
 */
@EntityType(EntityAggregates.class)
public class CommonEntityAggregatesDao2 extends CommonEntityDao2<EntityAggregates> implements IEntityDao2<EntityAggregates> {

    @Inject
    protected CommonEntityAggregatesDao2(final IFilter filter) {
	super(filter);
    }

    public int count(final AggregatedResultQueryModel model, final Map<String, Object> paramValues) {
	return evalNumOfPages(model, paramValues, 1);
    }

    public int count(final AggregatedResultQueryModel model) {
	return count(model, Collections.<String, Object> emptyMap());
    }

}
