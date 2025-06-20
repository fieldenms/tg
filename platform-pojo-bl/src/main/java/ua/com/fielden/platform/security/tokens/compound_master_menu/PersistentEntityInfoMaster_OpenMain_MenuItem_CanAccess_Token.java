package ua.com.fielden.platform.security.tokens.compound_master_menu;

import ua.com.fielden.platform.entity.PersistentEntityInfoMaster_OpenMain_MenuItem;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;

public class PersistentEntityInfoMaster_OpenMain_MenuItem_CanAccess_Token implements ISecurityToken {
    private final static String ENTITY_TITLE = TitlesDescsGetter.getEntityTitleAndDesc(PersistentEntityInfoMaster_OpenMain_MenuItem.class).getKey();
    public final static String TITLE = String.format(Template.MASTER_MENU_ITEM_ACCESS.forTitle(), ENTITY_TITLE);
    public final static String DESC = String.format(Template.MASTER_MENU_ITEM_ACCESS.forDesc(), ENTITY_TITLE);
}
