package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.utils.EntityUtils;

public interface ITgFuelType extends IEntityDao<TgFuelType> {

    static final String DEFAULT_VALUE_FOR_PROP_guarded = "default value";

    static final IFetchProvider<TgFuelType> FETCH_PROVIDER_FOR_EDITING = EntityUtils.fetch(TgFuelType.class).with(
            "key", "desc", "guardedIfPersisted", "guardedEvenIfNotPersisted");

}
