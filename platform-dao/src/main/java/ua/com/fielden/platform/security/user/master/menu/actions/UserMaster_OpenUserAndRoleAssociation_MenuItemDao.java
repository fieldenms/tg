package ua.com.fielden.platform.security.user.master.menu.actions;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.security.tokens.compound_master_menu.UserMaster_OpenUserAndRoleAssociation_MenuItem_CanAccess_Token;

/**
 * DAO implementation for companion object {@link UserMaster_OpenUserAndRoleAssociation_MenuItemCo}.
 *
 * @author TG Team
 *
 */
@EntityType(UserMaster_OpenUserAndRoleAssociation_MenuItem.class)
public class UserMaster_OpenUserAndRoleAssociation_MenuItemDao extends CommonEntityDao<UserMaster_OpenUserAndRoleAssociation_MenuItem> implements UserMaster_OpenUserAndRoleAssociation_MenuItemCo {

    @Inject
    public UserMaster_OpenUserAndRoleAssociation_MenuItemDao(final IFilter filter) {
        super(filter);
    }

    @Override
    @SessionRequired
    @Authorise(UserMaster_OpenUserAndRoleAssociation_MenuItem_CanAccess_Token.class)
    public UserMaster_OpenUserAndRoleAssociation_MenuItem save(final UserMaster_OpenUserAndRoleAssociation_MenuItem entity) {
        return super.save(entity);
    }

}