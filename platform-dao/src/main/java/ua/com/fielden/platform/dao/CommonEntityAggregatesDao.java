package ua.com.fielden.platform.dao;

import ua.com.fielden.platform.equery.EntityAggregates;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO implementation for EntityAggregates class used purely for internals of the EntityAggregatesDao.
 * @author TG Team
 *
 */
@EntityType(EntityAggregates.class)
public class CommonEntityAggregatesDao extends CommonEntityDao<EntityAggregates> implements IEntityDao<EntityAggregates> {

    @Inject
    protected CommonEntityAggregatesDao(final IFilter filter) {
	super(filter);
    }

}
