package ua.com.fielden.platform.menu;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 *
 * @author TG Team
 *
 */
@EntityType(UserMenuVisibilityAssociator.class)
public class UserMenuVisibilityAssociatorDao extends CommonEntityDao<UserMenuVisibilityAssociator> implements UserMenuVisibilityAssociatorCo {

    protected UserMenuVisibilityAssociatorDao(final IFilter filter) {
        super(filter);
    }

}
