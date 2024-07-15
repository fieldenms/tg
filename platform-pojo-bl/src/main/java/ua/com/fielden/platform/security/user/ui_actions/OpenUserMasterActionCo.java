package ua.com.fielden.platform.security.user.ui_actions;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * Companion object for entity {@link OpenUserMasterAction}.
 *
 * @author TG Team
 *
 */
public interface OpenUserMasterActionCo extends IEntityDao<OpenUserMasterAction> {

    static final IFetchProvider<OpenUserMasterAction> FETCH_PROVIDER = EntityUtils.fetch(OpenUserMasterAction.class).with(
        // key is needed to be correctly autopopulated by newly saved compound master entity (ID-based restoration of entity-typed key)
        "key");

}
