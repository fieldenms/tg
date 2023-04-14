package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * Companion object for entity {@link EntityMasterHelp}.
 *
 * @author TG Team
 *
 */
public interface EntityMasterHelpCo extends IEntityDao<EntityMasterHelp> {

    static final IFetchProvider<EntityMasterHelp> FETCH_PROVIDER = EntityUtils.fetch(EntityMasterHelp.class).with("entityType", "help");

}
