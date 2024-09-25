package ua.com.fielden.companion;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

@EntityType(Entity1.class)
class Entity1Dao extends CommonEntityDao<Entity1> implements Entity1Co {

    @Inject
    public Entity1Dao(final IFilter filter) {
        super(filter);
    }

}
