package ua.com.fielden.platform.security.tokens.compound_master_menu;

import ua.com.fielden.platform.entity.AuditCompoundMenuItem;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;

/// A security token for entity {@link AuditCompoundMenuItem} to guard Access.
///
public class AuditCompoundMenuItem_CanAccess_Token implements ISecurityToken {
    private final static String ENTITY_TITLE = TitlesDescsGetter.getEntityTitleAndDesc(AuditCompoundMenuItem.class).getKey();
    public final static String TITLE = String.format(Template.MASTER_MENU_ITEM_ACCESS.forTitle(), ENTITY_TITLE);
    public final static String DESC = String.format(Template.MASTER_MENU_ITEM_ACCESS.forDesc(), ENTITY_TITLE);
}
