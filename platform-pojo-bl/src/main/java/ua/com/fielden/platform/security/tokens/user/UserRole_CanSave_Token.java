package ua.com.fielden.platform.security.tokens.user;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;
import ua.com.fielden.platform.security.user.UserRole;

public class UserRole_CanSave_Token implements ISecurityToken {
    public final static String TITLE = String.format(Template.SAVE.forTitle(), UserRole.ENTITY_TITLE);
    public final static String DESC = String.format(Template.SAVE.forDesc(), UserRole.ENTITY_TITLE);
}