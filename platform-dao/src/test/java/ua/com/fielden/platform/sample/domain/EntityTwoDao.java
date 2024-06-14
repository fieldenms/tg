package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.IEntityTwo;

/**
 * DAO implementation for companion object {@link IEntityTwo}.
 *
 * @author Consultant/s
 *
 */
@EntityType(EntityTwo.class)
public class EntityTwoDao extends CommonEntityDao<EntityTwo> implements IEntityTwo {

    @Inject
    public EntityTwoDao(final IFilter filter) {
        super(filter);
    }

}