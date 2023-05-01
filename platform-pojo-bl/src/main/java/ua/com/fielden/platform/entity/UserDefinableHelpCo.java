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

    /**
     * Returns an existing help entity associated with {@code refElement}.
     * If it does not exist, but the default help hyperlink was defined, then returns a new instance populated with the default help.
     * Otherwise, returns a new instance without help.
     *
     * @param refElement
     * @return
     */
    UserDefinableHelp findOrNewWithDefaultHelp(final String refElement);
}