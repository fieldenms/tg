package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * Companion object for entity {@link UserDefinableHelp}.
 *
 * @author TG Team
 *
 */
public interface UserDefinableHelpCo extends IEntityDao<UserDefinableHelp> {

    static final IFetchProvider<UserDefinableHelp> FETCH_PROVIDER = EntityUtils.fetch(UserDefinableHelp.class).with("referenceElement", "help");

}
