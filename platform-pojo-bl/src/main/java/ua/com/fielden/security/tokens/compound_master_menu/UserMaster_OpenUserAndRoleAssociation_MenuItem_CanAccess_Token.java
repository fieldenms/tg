package ua.com.fielden.security.tokens.compound_master_menu;

import static java.lang.String.format;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;
import ua.com.fielden.platform.security.user.master.menu.actions.UserMaster_OpenUserAndRoleAssociation_MenuItem;

/**
 * A security token for entity {@link UserMaster_OpenUserAndRoleAssociation_MenuItem} to guard Access.
 *
 * @author TG Team
 *
 */
public class UserMaster_OpenUserAndRoleAssociation_MenuItem_CanAccess_Token implements ISecurityToken {
    public final static String TITLE = format(Template.MASTER_MENU_ITEM_ACCESS.forTitle(), UserMaster_OpenUserAndRoleAssociation_MenuItem.ENTITY_TITLE);
    public final static String DESC = format(Template.MASTER_MENU_ITEM_ACCESS.forDesc(), UserMaster_OpenUserAndRoleAssociation_MenuItem.ENTITY_TITLE);
}