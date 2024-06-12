package ua.com.fielden.platform.security.user.master.menu.actions;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.security.tokens.compound_master_menu.UserMaster_OpenMain_MenuItem_CanAccess_Token;

/**
 * DAO implementation for companion object {@link UserMaster_OpenMain_MenuItemCo}.
 *
 * @author TG Team
 */
@EntityType(UserMaster_OpenMain_MenuItem.class)
public class UserMaster_OpenMain_MenuItemDao extends CommonEntityDao<UserMaster_OpenMain_MenuItem> implements UserMaster_OpenMain_MenuItemCo {

    @Override
    @SessionRequired
    @Authorise(UserMaster_OpenMain_MenuItem_CanAccess_Token.class)
    public UserMaster_OpenMain_MenuItem save(final UserMaster_OpenMain_MenuItem entity) {
        return super.save(entity);
    }

}
