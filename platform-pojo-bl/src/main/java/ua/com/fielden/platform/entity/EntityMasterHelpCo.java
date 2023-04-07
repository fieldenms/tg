package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.dao.IEntityDao;

/**
 * Companion object for entity {@link EntityMasterHelp}.
 *
 * @author Developers
 *
 */
public interface EntityMasterHelpCo extends IEntityDao<EntityMasterHelp> {

    static final IFetchProvider<EntityMasterHelp> FETCH_PROVIDER = EntityUtils.fetch(EntityMasterHelp.class).with(
        // TODO: uncomment the following line and specify the properties, which are required for the UI. Then remove the line after.
        // "key", "desc");
        "Please specify the properties, which are required for the UI");

}
