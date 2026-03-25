package ua.com.fielden.platform.web.resources.webui.test_entities;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

@EntityType(Action2.class)
public class Action2Dao extends CommonEntityDao<Action2> implements Action2Co {

    @Override
    protected IFetchProvider<Action2> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}
