package ua.com.fielden.security.tokens.compound_master_menu;

import static java.lang.String.format;

import ua.com.fielden.platform.security.tokens.CompoundModuleToken;
import ua.com.fielden.platform.security.tokens.Template;
import ua.com.fielden.platform.security.user.master.menu.actions.UserMaster_OpenMain_MenuItem;

/**
 * A security token for entity {@link UserMaster_OpenMain_MenuItem} to guard Access.
 *
 * @author TG Team
 *
 */
public class UserMaster_OpenMain_MenuItem_CanAccess_Token extends CompoundModuleToken {
    public final static String TITLE = format(Template.MASTER_MENU_ITEM_ACCESS.forTitle(), UserMaster_OpenMain_MenuItem.ENTITY_TITLE);
    public final static String DESC = format(Template.MASTER_MENU_ITEM_ACCESS.forDesc(), UserMaster_OpenMain_MenuItem.ENTITY_TITLE);
}