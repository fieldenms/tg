package ua.com.fielden.platform.security.tokens.compound_master_menu;

import ua.com.fielden.platform.sample.domain.compound.master.menu.actions.TgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem;
import ua.com.fielden.platform.security.tokens.CompoundModuleToken;
import ua.com.fielden.platform.security.tokens.Template;

/**
 * A security token for entity {@link TgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem} to guard Access.
 *
 * @author TG Team
 *
 */
public class TgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem_CanAccess_Token extends CompoundModuleToken {
    public final static String TITLE = String.format(Template.MASTER_MENU_ITEM_ACCESS.forTitle(), TgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem.ENTITY_TITLE);
    public final static String DESC = String.format(Template.MASTER_MENU_ITEM_ACCESS.forDesc(), TgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem.ENTITY_TITLE);
}