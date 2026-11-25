package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.utils.EntityUtils;

public interface ITgBogie extends IEntityDao<TgBogie> {

    IFetchProvider<TgBogie> FETCH_PROVIDER = EntityUtils.fetch(TgBogie.class).with(
            "key", "desc", TgBogie.LOCATION);

}
