package ua.com.fielden.platform.rao;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;

import java.util.Map;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * RAO implementation for EntityAggregates class used purely for internals of the EntityAggregatesRao.
 * 
 * @author TG Team
 * 
 */
@EntityType(EntityAggregates.class)
public class CommonEntityAggregatesRao extends CommonEntityRao<EntityAggregates> implements IEntityDao<EntityAggregates> {

    @Inject
    public CommonEntityAggregatesRao(final RestClientUtil restUtil) {
        super(restUtil);
    }

    public int count(final AggregatedResultQueryModel model, final Map<String, Object> paramValues) {
        return count(from(model).with(paramValues).model());
    }
}