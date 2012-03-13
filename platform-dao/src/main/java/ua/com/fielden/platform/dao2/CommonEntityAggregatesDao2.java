package ua.com.fielden.platform.dao2;

import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.IFilter;
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
}
