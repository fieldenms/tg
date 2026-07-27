package ua.com.fielden.platform.share;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

@EntityType(ShareAction.class)
public class ShareActionDao extends CommonEntityDao<ShareAction> implements ShareActionCo {

    @Override
    protected IFetchProvider<ShareAction> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}
