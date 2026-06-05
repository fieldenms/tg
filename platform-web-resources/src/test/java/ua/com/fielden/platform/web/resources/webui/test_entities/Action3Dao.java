package ua.com.fielden.platform.web.resources.webui.test_entities;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

@EntityType(Action3.class)
public class Action3Dao extends CommonEntityDao<Action3> implements Action3Co {

    @Override
    protected IFetchProvider<Action3> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}
