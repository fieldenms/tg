package ua.com.fielden.platform.web.resources.webui.test_entities;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

import java.util.Set;

import static ua.com.fielden.platform.utils.EntityUtils.fetch;

public interface Action1Co extends IEntityDao<Action1> {

    IFetchProvider<Action1> FETCH_PROVIDER = fetch(Action1.class).with(
            Set.of(Action1.Properties.values()));

}
