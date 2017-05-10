package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.IEntityOne;

/**
 * DAO implementation for companion object {@link IEntityOne}.
 *
 * @author Consultant/s
 *
 */
@EntityType(EntityOne.class)
public class EntityOneDao extends CommonEntityDao<EntityOne> implements IEntityOne {

    @Inject
    public EntityOneDao(final IFilter filter) {
        super(filter);
    }

}