package ua.com.fielden.platform.security.tokens.user;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;
import ua.com.fielden.platform.security.user.UserRole;

public class UserRole_CanDelete_Token implements ISecurityToken {
    public final static String TITLE = String.format(Template.DELETE.forTitle(), UserRole.ENTITY_TITLE);
    public final static String DESC = String.format(Template.DELETE.forDesc(), UserRole.ENTITY_TITLE);
}