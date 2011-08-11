package ua.com.fielden.platform.rao;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.equery.EntityAggregates;
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
    private static final long serialVersionUID = 1L;

    @Inject
    public CommonEntityAggregatesRao(final RestClientUtil restUtil) {
	super(restUtil);
    }
}
