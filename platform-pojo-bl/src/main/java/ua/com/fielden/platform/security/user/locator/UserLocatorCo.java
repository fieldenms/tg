package ua.com.fielden.platform.security.user.locator;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * Companion object for entity {@link UserLocator}.
 *
 * @author TG Team
 *
 */
public interface UserLocatorCo extends IEntityDao<UserLocator> {

    static final IFetchProvider<UserLocator> FETCH_PROVIDER = EntityUtils.fetch(UserLocator.class).with("user");

}