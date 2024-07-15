package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.EntityThreeCo;

/**
 * DAO implementation for companion object {@link EntityThreeCo}.
 *
 * @author TG Team
 *
 */
@EntityType(EntityThree.class)
public class EntityThreeDao extends CommonEntityDao<EntityThree> implements EntityThreeCo {

    @Inject
    public EntityThreeDao(final IFilter filter) {
        super(filter);
    }

}