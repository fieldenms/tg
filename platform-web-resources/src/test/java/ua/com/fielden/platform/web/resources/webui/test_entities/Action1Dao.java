package ua.com.fielden.platform.web.resources.webui.test_entities;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

@EntityType(Action1.class)
public class Action1Dao extends CommonEntityDao<Action1> implements Action1Co {

    @Override
    protected IFetchProvider<Action1> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}
