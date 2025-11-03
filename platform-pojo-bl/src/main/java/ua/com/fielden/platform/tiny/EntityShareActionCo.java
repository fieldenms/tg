package ua.com.fielden.platform.tiny;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

import static ua.com.fielden.platform.tiny.EntityShareAction.HYPERLINK;
import static ua.com.fielden.platform.utils.EntityUtils.fetch;

public interface EntityShareActionCo extends IEntityDao<EntityShareAction> {

    IFetchProvider<EntityShareAction> FETCH_PROVIDER = fetch(EntityShareAction.class).with(
            HYPERLINK);

}
