package ua.com.fielden.platform.tiny;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

@EntityType(EntityShareAction.class)
public class EntityShareActionDao extends CommonEntityDao<EntityShareAction> implements EntityShareActionCo {

    @Override
    protected IFetchProvider<EntityShareAction> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}
