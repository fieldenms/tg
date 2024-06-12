package ua.com.fielden.platform.security.user.locator;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link UserLocatorCo}.
 *
 * @author TG Team
 */
@EntityType(UserLocator.class)
public class UserLocatorDao extends CommonEntityDao<UserLocator> implements UserLocatorCo {

    @Override
    protected IFetchProvider<UserLocator> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}
