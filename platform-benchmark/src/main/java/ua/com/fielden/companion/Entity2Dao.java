package ua.com.fielden.companion;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.IEntityAggregatesOperations;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.utils.IDates;

@EntityType(Entity2.class)
class Entity2Dao extends CommonEntityDao<Entity2> implements Entity2Co {

    private final IUserProvider userProvider;
    private final IEntityAggregatesOperations aggregatesOps;
    private final IUser userCo;
    private final IDates dates;

    @Inject
    public Entity2Dao(final IFilter filter,
                      final IUserProvider userProvider,
                      final IEntityAggregatesOperations aggregatesOps,
                      final IUser userCo,
                      final IDates dates) {
        super(filter);
        this.userProvider = userProvider;
        this.aggregatesOps = aggregatesOps;
        this.userCo = userCo;
        this.dates = dates;
    }

}
