package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

import static ua.com.fielden.platform.utils.EntityUtils.fetch;

/// DAO implementation for companion object [TgEntityWithIsPropertyOverriddenIdCo].
///
@EntityType(TgEntityWithIsPropertyOverriddenId.class)
public class TgEntityWithIsPropertyOverriddenIdDao
    extends CommonEntityDao<TgEntityWithIsPropertyOverriddenId>
    implements TgEntityWithIsPropertyOverriddenIdCo {

    @Override
    protected IFetchProvider<TgEntityWithIsPropertyOverriddenId> createFetchProvider() {
        return fetch(TgEntityWithIsPropertyOverriddenId.class).with("key");
    }

}