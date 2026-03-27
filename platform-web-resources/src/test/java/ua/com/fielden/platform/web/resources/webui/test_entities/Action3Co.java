package ua.com.fielden.platform.web.resources.webui.test_entities;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

import java.util.Set;

import static ua.com.fielden.platform.utils.EntityUtils.fetch;

public interface Action3Co extends IEntityDao<Action3> {

    IFetchProvider<Action3> FETCH_PROVIDER = fetch(Action3.class).with(
            Set.of(Action3.Properties.values()));

}
