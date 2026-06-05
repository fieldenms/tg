package ua.com.fielden.platform.web.resources.webui.test_entities;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

import java.util.Set;

import static ua.com.fielden.platform.utils.EntityUtils.fetch;

public interface Action2Co extends IEntityDao<Action2> {

    IFetchProvider<Action2> FETCH_PROVIDER = fetch(Action2.class).with(
            Set.of(Action2.Properties.values()));

}
